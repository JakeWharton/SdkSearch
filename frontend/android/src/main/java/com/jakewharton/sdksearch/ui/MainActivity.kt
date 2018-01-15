package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.app.ShareCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.INVISIBLE
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.db.DbComponent
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.sync.ItemSynchronizer
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
  private val baseUrl = BaseUrl(PRODUCTION_DAC)
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

    val synchronizer = ItemSynchronizer(store, service, ITEM_LIST_URL_PATHS)
    val androidReference = AndroidReference(PRODUCTION_GIT_WEB)

    val onClick = { item: Item ->
      val uri = baseUrl.resolve(item.link()).toUri()
      val sourceUri = androidReference.sourceUrl(item.package_(), item.class_())?.toUri()
      CustomTabsIntent.Builder()
          .setToolbarColor(getColor(R.color.green))
          .addDefaultShareMenuItem()
          .apply {
            if (sourceUri != null) {
              val sourceIntent = Intent(ACTION_VIEW, sourceUri)
              val pendingIntent = PendingIntent.getActivity(this@MainActivity, 123, sourceIntent, 0)
              addMenuItem(getString(R.string.view_class_source, item.class_()), pendingIntent)
            }
          }
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
    val onSource = { item: Item ->
      val url = androidReference.sourceUrl(item.package_(), item.class_())
      if (url != null) {
        CustomTabsIntent.Builder()
            .setToolbarColor(getColor(R.color.green))
            .addDefaultShareMenuItem()
            .build()
            .launchUrl(this, url.toUri())
      } else {
        Toast.makeText(this@MainActivity, R.string.unknown_source, LENGTH_SHORT).show()
      }
    }

    setContentView(R.layout.main)

    val recycler = findViewById<RecyclerView>(R.id.results)
    val layoutManager = LinearLayoutManager(this)
    recycler.layoutManager = layoutManager
    val adapter = ItemAdapter(layoutInflater, onClick, onCopy, onShare, onSource)
    recycler.adapter = adapter

    val queryInput = findViewById<EditText>(R.id.query)

    store.count()
        .observeOn(mainThread())
        .subscribe({
          queryInput.hint = resources.getQuantityString(R.plurals.search_classes, it.toInt(), it)
        }, {
          throw OnErrorNotImplementedException(it)
        })
        .addTo(disposables)

    queryInput.textChanges()
        .map(CharSequence::toString)
        .switchMap { query ->
          val results = if (query.isBlank()) just(emptyList())
          else store.queryItems(query).delaySubscription(200, MILLISECONDS, mainThread())

          results.map { query to it }
        }
        .observeOn(computation())
        .scan(QueryResults()) { (oldQuery, oldItems), (newQuery, newItems) ->
          val diff = DiffUtil.calculateDiff(ItemDiffer(oldQuery, oldItems, newQuery, newItems))
          QueryResults(newQuery, newItems, diff)
        }
        .skip(1)
        .observeOn(mainThread())
        .subscribe({
          val scrollPosition = layoutManager.findFirstVisibleItemPosition()
          adapter.updateItems(it.query, it.data)
          it.diff.dispatchUpdatesTo(adapter)
          recycler.scrollToPosition(scrollPosition)
        }, {
          throw OnErrorNotImplementedException(it)
        })
        .addTo(disposables)

    val clear = findViewById<View>(R.id.clear_query)
    clear.setOnClickListener {
      queryInput.setText("")
    }

    queryInput.textChanges()
        .map(CharSequence::isNotEmpty)
        .subscribe(clear.visibility(INVISIBLE), Consumer {
          throw OnErrorNotImplementedException(it)
        })
        .addTo(disposables)

    launch(UI) {
      var snackbar: Snackbar? = null
      for (state in synchronizer.state) {
        if (state.isNotEmpty()) {
          val failed = state.count { it.failed }
          val updating = state.size - failed

          val message = when {
            failed == 0 -> {
              resources.getQuantityString(R.plurals.updating, updating, updating)
            }
            updating == 0 -> {
              resources.getQuantityString(R.plurals.updating_failed, failed, failed)
            }
            else -> {
              resources.getQuantityString(R.plurals.updating_with_failed, updating, updating, failed)
            }
          }

          if (snackbar == null) {
            snackbar = Snackbar.make(recycler, message, LENGTH_INDEFINITE)
            snackbar.show()
          } else {
            snackbar.setText(message)
          }

          if (failed > 0 && updating == 0) {
            snackbar.setAction(R.string.dismiss) {
              snackbar.dismiss()
            }
          }
        } else {
          snackbar?.dismiss()
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
