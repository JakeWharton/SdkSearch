package com.jakewharton.sdksearch.ui

import android.graphics.Typeface
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.EditText
import androidx.content.systemService
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event.ClearSyncStatus
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model.QueryResults
import com.jakewharton.sdksearch.util.layoutInflater
import com.jakewharton.sdksearch.util.onEditorAction
import com.jakewharton.sdksearch.util.onKey
import com.jakewharton.sdksearch.util.onScroll
import com.jakewharton.sdksearch.util.onTextChanged
import io.reactivex.functions.Consumer
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

class SearchViewBinder(
  view: View,
  private val onClick: ItemHandler,
  private val onCopy: ItemHandler,
  private val onShare: ItemHandler,
  private val onSource: ItemHandler
) {
  private val context = view.context

  private val resultsAdapter = ItemAdapter(context.layoutInflater, object : ItemAdapter.Callback {
    override fun onItemClicked(item: Item) = onClick(item)
    override fun onItemCopied(item: Item) = onCopy(item)
    override fun onItemShared(item: Item) = onShare(item)
    override fun onItemViewSource(item: Item) = onSource(item)
  })

  private val results: RecyclerView = view.findViewById(R.id.results)
  private val queryInput: EditText = view.findViewById(R.id.query)
  private val queryClear: View = view.findViewById(R.id.clear_query)

  init {
    results.adapter = resultsAdapter

    val layoutManager = LinearLayoutManager(context)
    results.layoutManager = layoutManager

    val dividerDecoration = DividerItemDecoration(context, layoutManager.orientation)
    dividerDecoration.setDrawable(context.getDrawable(R.drawable.list_divider))
    results.addItemDecoration(dividerDecoration)

    queryClear.setOnClickListener {
      queryInput.setText("")
    }

    val robotoMono = ResourcesCompat.getFont(context, R.font.roboto_mono)
    queryInput.onTextChanged {
      queryClear.visibility = if (it.isEmpty()) INVISIBLE else VISIBLE
      queryInput.typeface = if (it.isEmpty()) Typeface.DEFAULT else robotoMono
    }

    val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var totalDy = 0
    results.onScroll { _, dy ->
      if (dy > 0) {
        totalDy += dy
        if (totalDy >= touchSlop) {
          totalDy = 0

          val inputMethodManager = context.systemService<InputMethodManager>()
          inputMethodManager.hideSoftInputFromWindow(queryInput.windowToken, HIDE_NOT_ALWAYS)
        }
      }
    }

    queryInput.onKey {
      if (it.keyCode == KeyEvent.KEYCODE_ENTER) {
        resultsAdapter.invokeFirstItem()
        true
      } else {
        false
      }
    }
    queryInput.onEditorAction {
      if (it == EditorInfo.IME_ACTION_GO) {
        resultsAdapter.invokeFirstItem()
        true
      } else {
        false
      }
    }
  }

  fun init(args: Args) {
    args.defaultQuery?.let(queryInput::setText)
  }

  fun attach(models: ReceiveChannel<Model>, events: Consumer<Event>): Job {
    val resources = context.resources
    val parentJob = Job()

    val watcher = queryInput.onTextChanged {
      events.accept(Event.QueryChanged(it.toString()))
    }
    parentJob.invokeOnCompletion {
      queryInput.removeTextChangedListener(watcher)
    }

    val queryResultProcessor = actor<QueryResults>(CommonPool, parent = parentJob) {
      var oldResults = receive()

      launch(UI) {
        resultsAdapter.updateItems(oldResults)
      }

      consumeEach { newResults ->
        val diff = DiffUtil.calculateDiff(ItemDiffer(oldResults, newResults))

        launch(UI) {
          resultsAdapter.updateItems(newResults)
          diff.dispatchUpdatesTo(resultsAdapter)

          // Always reset the scroll position to the top when the query changes.
          results.scrollToPosition(0)
        }

        oldResults = newResults
      }
    }

    launch(Unconfined, parent = parentJob) {
      var snackbar: Snackbar? = null

      for (model in models) {
        val count = model.count
        queryInput.hint = resources.getQuantityString(R.plurals.search_classes, count.toInt(), count)

        queryResultProcessor.offer(model.queryResults)

        val (inFlight, failed) = model.syncStatus
        if (inFlight != 0 || failed != 0) {
          val message = when {
            failed == 0 -> {
              resources.getQuantityString(R.plurals.updating, inFlight, inFlight)
            }
            inFlight == 0 -> {
              resources.getQuantityString(R.plurals.updating_failed, failed, failed)
            }
            else -> {
              resources.getQuantityString(R.plurals.updating_with_failed, inFlight, inFlight, failed)
            }
          }

          if (snackbar == null) {
            snackbar = Snackbar.make(results, message, LENGTH_INDEFINITE)
            snackbar.show()
          } else {
            snackbar.setText(message)
          }

          if (failed > 0 && inFlight == 0) {
            snackbar.setAction(R.string.dismiss) {
              events.accept(ClearSyncStatus)
            }
          }
        } else {
          snackbar?.dismiss()
        }
      }
    }

    return parentJob
  }

  data class Args(val defaultQuery: String? = null)
}
