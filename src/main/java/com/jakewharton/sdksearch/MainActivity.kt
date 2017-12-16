package com.jakewharton.sdksearch

import android.app.Activity
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.os.Bundle
import android.widget.TextView
import com.jakewharton.sdksearch.api.ApiComponent
import com.jakewharton.sdksearch.api.DocumentationService
import com.jakewharton.sdksearch.db.DbCallback
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.db.ItemModel
import com.jakewharton.sdksearch.db.ItemType
import com.squareup.sqlbrite3.BriteDatabase
import com.squareup.sqlbrite3.SqlBrite
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import timber.log.Timber

class MainActivity : Activity() {
  private val baseUrl = HttpUrl.parse("https://developer.android.com")!!
  private lateinit var service: DocumentationService
  private lateinit var disposable: Disposable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    service = ApiComponent.builder()
        .baseUrl(baseUrl)
        .build()
        .documentationService()

    setContentView(R.layout.main)
    val count = findViewById<TextView>(R.id.count)

    val dbConfiguration = Configuration.builder(this)
        .name("sdk.db")
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
    Timber.d("Listing $listing...")
    service.list(url).subscribe({ items ->
      Timber.d("Listing $listing got ${items.size} items")

      try {
        briteDatabase.newTransaction().use {
          Item.FACTORY.clear_listing(listing).let {
            briteDatabase.executeAndTrigger(it.tables, it.statement, *it.args)
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
    }, {
      runOnUiThread { throw RuntimeException(it) }
    })
  }
}
