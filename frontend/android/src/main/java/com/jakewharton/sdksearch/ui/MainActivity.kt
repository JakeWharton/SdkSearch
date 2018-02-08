package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.EditText
import android.widget.TextView
import androidx.content.systemService
import com.jakewharton.rxbinding2.support.v7.widget.scrollEvents
import com.jakewharton.rxbinding2.view.keys
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.editorActionEvents
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.store.DbComponent
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.ITEM_LIST_URL_PATHS
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.computation
import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS

class MainActivity : Activity() {
  private val baseUrl = BaseUrl(PRODUCTION_DAC)
  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT == 26) {
      // This attribute can only be set in code on API 26. It's in the theme in 27+.
      window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

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

    val onClick = OpenDocumentationItemHandler(this, baseUrl, androidReference)
    val onCopy = ClipboardCopyItemHandler(this, baseUrl)
    val onShare = ShareItemHandler(this, baseUrl)
    val onSource = OpenSourceItemHandler(this, androidReference)

    setContentView(R.layout.main)

    val recycler = findViewById<RecyclerView>(R.id.results)
    val layoutManager = LinearLayoutManager(this)
    recycler.layoutManager = layoutManager
    val adapter = ItemAdapter(layoutInflater, onClick, onCopy, onShare, onSource)
    recycler.adapter = adapter

    val dividerDecoration = DividerItemDecoration(recycler.context, layoutManager.orientation)
    dividerDecoration.setDrawable(getDrawable(R.drawable.list_divider))
    recycler.addItemDecoration(dividerDecoration)

    val emptyStateTextView = findViewById<TextView>(R.id.emptyState)

    val queryInput = findViewById<EditText>(R.id.query)
    if (savedInstanceState == null) {
      queryInput.setText(intent.getStringExtra("query") ?: "")
    }
    queryInput.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
      override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
          emptyStateTextView.text = getString(R.string.enter_class_name)
          emptyStateTextView.animate()
                  .scaleX(1f)
                  .scaleY(1f)
                  .alpha(1f)
                  .start()
        } else {
          emptyStateTextView.animate()
                  .scaleX(0f)
                  .scaleY(0f)
                  .alpha(0f)
                  .start()
        }
      }
    })
    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        if (queryInput.length() > 0 && adapter.itemCount == 0) {
          emptyStateTextView.visibility = View.VISIBLE
          emptyStateTextView.text = getString(R.string.no_results)
          emptyStateTextView.animate()
                  .scaleX(1f)
                  .scaleY(1f)
                  .alpha(1f)
                  .start()
        }
      }
    })

    val enterKeys = queryInput.keys(Predicate { it.keyCode == KEYCODE_ENTER })
        .filter { it.keyCode == KEYCODE_ENTER }
    val imeActions = queryInput.editorActionEvents()
        .filter { it.actionId() == IME_ACTION_GO }
    merge(enterKeys, imeActions)
        .crashingSubscribe{
          adapter.invokeFirstItem()
        }

    store.count()
        .observeOn(mainThread())
        .crashingSubscribe {
          queryInput.hint = resources.getQuantityString(R.plurals.search_classes, it.toInt(), it)
        }

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
        .crashingSubscribe {
          adapter.updateItems(it.query, it.data)
          it.diff.dispatchUpdatesTo(adapter)

          // Always reset the scroll position to the top when the query changes.
          recycler.scrollToPosition(0)
        }

    val clear = findViewById<View>(R.id.clear_query)
    clear.setOnClickListener {
      queryInput.setText("")
    }

    queryInput.textChanges()
        .map(CharSequence::isNotEmpty)
        .crashingSubscribe(clear.visibility(INVISIBLE))

    val robotoMono = ResourcesCompat.getFont(this, R.font.roboto_mono)
    queryInput.textChanges()
        .map(CharSequence::isEmpty)
        .crashingSubscribe { empty ->
          queryInput.typeface = if (empty) Typeface.DEFAULT else robotoMono
        }

    var totalDy = 0
    val vc = ViewConfiguration.get(recycler.context)
    val slop = vc.scaledTouchSlop
    recycler.scrollEvents()
        .filter { scroll -> scroll.dy() > 0 }
        .crashingSubscribe { scroll ->
          totalDy += scroll.dy()
          if (totalDy >= slop) {
            totalDy = 0
            systemService<InputMethodManager>().hideSoftInputFromWindow(currentFocus?.windowToken, HIDE_NOT_ALWAYS)
          }
        }

    launch(UI, UNDISPATCHED) {
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

  @Suppress("NOTHING_TO_INLINE") // Needed for correct stacktraces.
  private inline fun <I> Observable<I>.crashingSubscribe(noinline onNext: (I) -> Unit) {
    subscribe(onNext, { throw OnErrorNotImplementedException(it) }).addTo(disposables)
  }

  @Suppress("NOTHING_TO_INLINE") // Needed for correct stacktraces.
  private inline fun <I> Observable<I>.crashingSubscribe(onNext: Consumer<in I>) {
    subscribe(onNext, Consumer { throw OnErrorNotImplementedException(it) }).addTo(disposables)
  }

  private fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
  }
}
