package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.REFERENCE_LISTS
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.api.dac.DocumentationService
import com.jakewharton.sdksearch.db.DbComponent
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.db.ItemStore
import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import timber.log.Timber

class MainActivity : Activity() {
  private val baseUrl = HttpUrl.parse("https://developer.android.com")!!
  private val disposables = CompositeDisposable()
  private lateinit var service: DocumentationService
  private lateinit var store: ItemStore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    service = DacComponent.builder()
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
    store.count()
        .subscribe({ count.text = "Count: $it" }, { throw OnErrorNotImplementedException(it) })
        .addTo(disposables)

    val recycler = findViewById<RecyclerView>(R.id.results)
    val adapter = ItemAdapter(layoutInflater) {
      val uri = baseUrl.resolve(it.link())!!.toUri()
      startActivity(Intent(ACTION_VIEW, uri))
    }
    recycler.adapter = adapter

    val query = findViewById<EditText>(R.id.query)
    query.textChanges()
        .map(CharSequence::toString)
        .switchMap {
          if (it.isBlank()) just(emptyList())
          else store.queryItems(it).observeOn(AndroidSchedulers.mainThread())
        }
        // TODO Use DiffUtil to previous list.
        .subscribe({ adapter.updateItems(it) }, { throw OnErrorNotImplementedException(it) })
        .addTo(disposables)

    for ((listing, url) in REFERENCE_LISTS) {
      load(listing, url)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  private fun load(listing: String, url: String) {
    Timber.d("Listing $listing...")
    service.list(url).subscribe({ apiItems ->
      Timber.d("Listing $listing got ${apiItems.size} items")
      val dbItems = apiItems
          .filter { it.type == "class" }
          .map { Item.createForInsert(listing, it.label, it.link) }
      store.updateListing(listing, dbItems)
    }, {
      runOnUiThread { throw RuntimeException(it) }
    })
  }

  private fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
  }

  private fun HttpUrl.toUri(): Uri = Uri.parse(toString())
}
