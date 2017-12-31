package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.ShareCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.INVISIBLE
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.REFERENCE_LISTS
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.db.DbComponent
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import com.jakewharton.sdksearch.util.DataWithDiff
import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.computation
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS

class MainActivity : Activity() {
  private val baseUrl = BaseUrl("https://developer.android.com")
  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if ("true" == intent.getStringExtra("crash")) {
      Timber.e("Synthetic crash signal detected. Throwing in 3.. 2.. 1..")
      throw RuntimeException("Crash! Bang! Pow! This is only a test...")
    }

    val service = DacComponent.builder()
        .baseUrl(baseUrl)
        .build()
        .documentationService()

    val store = DbComponent.builder()
        .context(this)
        .scheduler(Schedulers.io())
        .filename("sdk.db")
        .build()
        .itemStore()

    val synchronizer = ItemSynchronizer(store, service, REFERENCE_LISTS)

    val onClick = { item: Item ->
      val uri = baseUrl.resolve(item.link()).toUri()
      CustomTabsIntent.Builder()
          .setToolbarColor(getColor(R.color.primary))
          .addDefaultShareMenuItem()
          .build()
          .launchUrl(this, uri)
    }
    val onCopy = { item: Item ->
      val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
      val uri = baseUrl.resolve(item.link()).toUri()
      clipboard.primaryClip = ClipData.newPlainText(item.class_(), uri.toString())
      val message = getString(R.string.copied, item.class_())
      Toast.makeText(this, message, LENGTH_SHORT).show()
    }
    val onShare = { item: Item ->
      val uri = baseUrl.resolve(item.link()).toUri()
      ShareCompat.IntentBuilder.from(this)
          .setType("text/plain")
          .setChooserTitle(getString(R.string.share_title, item.class_()))
          .setText(uri.toString())
          .startChooser()
    }

    setContentView(R.layout.main)

    val recycler = findViewById<RecyclerView>(R.id.results)
    val layoutManager = LinearLayoutManager(this)
    recycler.layoutManager = layoutManager
    val adapter = ItemAdapter(layoutInflater, onClick, onCopy, onShare)
    recycler.adapter = adapter

    val query = findViewById<EditText>(R.id.query)

    store.count()
        .observeOn(mainThread())
        .subscribe({
          query.hint = resources.getQuantityString(R.plurals.search_classes, it.toInt(), it)
        }, {
          throw OnErrorNotImplementedException(it)
        })
        .addTo(disposables)

    query.textChanges()
        .map(CharSequence::toString)
        .switchMap {
          if (it.isBlank()) just(emptyList())
          else store.queryItems(it).delaySubscription(200, MILLISECONDS, mainThread())
        }
        .observeOn(computation())
        .scan(DataWithDiff(emptyList<Item>())) { (old), new ->
          DataWithDiff(new, DiffUtil.calculateDiff(ItemDiffCallback(old, new)))
        }
        .skip(1)
        .observeOn(mainThread())
        .subscribe({
          val scrollPosition = layoutManager.findFirstVisibleItemPosition()
          adapter.updateItems(it.data)
          it.diff.dispatchUpdatesTo(adapter)
          recycler.scrollToPosition(scrollPosition)
        }, {
          throw OnErrorNotImplementedException(it)
        })
        .addTo(disposables)

    val clear = findViewById<View>(R.id.clear_query)
    clear.setOnClickListener {
      query.setText("")
    }

    query.textChanges()
        .map(CharSequence::isNotEmpty)
        .subscribe(clear.visibility(INVISIBLE), Consumer {
          throw OnErrorNotImplementedException(it)
        })
        .addTo(disposables)

    launch(UI) {
      var toast: Toast? = null
      for (isRunning in synchronizer.isRunning) {
        if (isRunning) {
          toast = Toast.makeText(this@MainActivity, "Updating...", LENGTH_LONG)
          toast.show()
        } else {
          toast?.cancel()
        }
      }
    }

    synchronizer.forceSync()
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  private fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
  }

  private fun String.toUri(): Uri = Uri.parse(this)
}
