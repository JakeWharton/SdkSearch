package com.jakewharton.sdksearch

import android.app.Activity
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.os.Bundle
import android.widget.TextView
import com.jakewharton.sdksearch.api.ApiItem
import com.jakewharton.sdksearch.db.DbCallback
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.db.ItemModel
import com.jakewharton.sdksearch.db.ItemType
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.sqlbrite3.BriteDatabase
import com.squareup.sqlbrite3.SqlBrite
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.ByteString
import timber.log.Timber
import java.io.IOException

class MainActivity : Activity() {
  private val baseUrl = HttpUrl.parse("https://developer.android.com")!!
  private val moshi = Moshi.Builder().build()
  private val client = OkHttpClient()
  private lateinit var disposable: Disposable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    val count = findViewById<TextView>(R.id.count)

    val dbConfiguration = Configuration.builder(this)
        // .name("sdk.db") Use in-memory DB for now.
        .callback(DbCallback())
        .build()
    val openHelper = FrameworkSQLiteOpenHelperFactory().create(dbConfiguration)
    val sqlBrite = SqlBrite.Builder().logger { Timber.tag("Database").d(it) }.build()
    val briteDatabase = sqlBrite.wrapDatabaseHelper(openHelper, Schedulers.io())

    disposable = Item.FACTORY.count().let { briteDatabase.createQuery(it.tables, it.statement) }
        .mapToOne(Item.FACTORY.countMapper()::map)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ count.text = "Count: $it" }, { throw OnErrorNotImplementedException(it) })

    for ((listing, url) in REFERENCE_LISTS) {
      load(listing, url, briteDatabase)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.dispose()
  }

  private fun load(listing: String, url: String, briteDatabase: BriteDatabase) {
    val request = Request.Builder()
        .url(baseUrl.resolve(url)!!)
        .build()
    val call = client.newCall(request)
    Timber.d("Listing $listing...")
    call.enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        if (!response.isSuccessful) {
          runOnUiThread { throw RuntimeException("HTTP ${response.code()}") }
          return
        }
        val source = response.body()!!.source()
        val start = source.indexOf(ByteString.encodeUtf8("["))
        source.skip(start)

        val type = Types.newParameterizedType(List::class.java, ApiItem::class.java)
        val adapter = moshi.adapter<List<ApiItem>>(type)
        val reader = JsonReader.of(source)
        reader.isLenient = true // Keys are unquoted.

        val items = adapter.fromJson(reader)!!
        Timber.d("Listing $listing got ${items.size} items")

        try {
          briteDatabase.newTransaction().use {
            Item.FACTORY.clear_listing(listing).let {
              briteDatabase.executeAndTrigger(it.tables, it.statement)
            }
            for (item in items) {
              briteDatabase.insert(ItemModel.TABLE_NAME, 0, Item.FACTORY.marshal()
                  .listing(listing)
                  .label(item.label)
                  .link(item.link)
                  .type(ItemType.parse(item.type))
                  .asContentValues())
            }
            it.markSuccessful()
          }
        } catch (e: RuntimeException) {
          Timber.e(e)
        } catch (e: Exception) {
          Timber.e(e)
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        runOnUiThread { throw RuntimeException(e) }
      }
    })
  }
}
