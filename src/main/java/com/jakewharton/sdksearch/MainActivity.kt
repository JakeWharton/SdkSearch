package com.jakewharton.sdksearch

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.jakewharton.sdksearch.api.ApiComponent
import com.jakewharton.sdksearch.api.DocumentationService
import com.jakewharton.sdksearch.db.DbComponent
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.db.ItemStore
import com.jakewharton.sdksearch.db.ItemType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import timber.log.Timber

class MainActivity : Activity() {
  private val baseUrl = HttpUrl.parse("https://developer.android.com")!!
  private lateinit var service: DocumentationService
  private lateinit var store: ItemStore
  private lateinit var disposable: Disposable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    service = ApiComponent.builder()
        .baseUrl(baseUrl)
        .build()
        .documentationService()

    store = DbComponent.builder()
        .context(this)
        .scheduler(Schedulers.io())
        .filename("sdk.db")
        .build()
        .itemStore()

    setContentView(R.layout.main)
    val count = findViewById<TextView>(R.id.count)

    disposable = store.count()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ count.text = "Count: $it" }, { throw OnErrorNotImplementedException(it) })

    for ((listing, url) in REFERENCE_LISTS) {
      load(listing, url)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.dispose()
  }

  private fun load(listing: String, url: String) {
    Timber.d("Listing $listing...")
    service.list(url).subscribe({ apiItems ->
      Timber.d("Listing $listing got ${apiItems.size} items")
      val dbItems = apiItems
          .map { Item.createForInsert(listing, it.label, ItemType.parse(it.type), it.link) }
      store.updateListing(listing, dbItems)
    }, {
      runOnUiThread { throw RuntimeException(it) }
    })
  }
}
