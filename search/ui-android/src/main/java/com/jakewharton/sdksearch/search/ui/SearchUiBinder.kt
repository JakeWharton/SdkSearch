package com.jakewharton.sdksearch.search.ui

import android.graphics.Typeface
import android.view.KeyEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.EditText
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.jakewharton.presentation.UiBinder
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Event.ClearSyncStatus
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model.SyncStatus.FAILED
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model.SyncStatus.IDLE
import com.jakewharton.sdksearch.search.presenter.SearchPresenter.Model.SyncStatus.SYNC
import com.jakewharton.sdksearch.search.ui.util.layoutInflater
import com.jakewharton.sdksearch.search.ui.util.onEditorAction
import com.jakewharton.sdksearch.search.ui.util.onKey
import com.jakewharton.sdksearch.search.ui.util.onScroll
import com.jakewharton.sdksearch.search.ui.util.onTextChanged
import com.jakewharton.sdksearch.store.item.Item
import kotlinx.coroutines.channels.SendChannel
import com.jakewharton.sdksearch.roboto.R as RobotoR

class SearchUiBinder(
  view: View,
  private val events: SendChannel<Event>,
  private val onClick: ItemHandler,
  private val onCopy: ItemHandler,
  private val onShare: ItemHandler,
  private val onSource: ItemHandler
) : UiBinder<Model> {
  private val context = view.context
  private val resources = view.resources

  private val results: RecyclerView = view.findViewById(R.id.results)
  private val queryInput: EditText = view.findViewById(R.id.query)
  private val queryClear: View = view.findViewById(R.id.clear_query)

  private val resultsAdapter = ItemResultAdapter(context.layoutInflater, object : ItemResultAdapter.Callback {
    override fun onItemClicked(item: Item) = onClick(item)
    override fun onItemCopied(item: Item) = onCopy(item)
    override fun onItemShared(item: Item) = onShare(item)
    override fun onItemViewSource(item: Item) = onSource(item)
  })

  private var snackbar: Snackbar? = null

  init {
    results.adapter = resultsAdapter

    val layoutManager = LinearLayoutManager(context)
    results.layoutManager = layoutManager
    results.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

    TooltipCompat.setTooltipText(queryClear, queryClear.contentDescription)
    queryClear.setOnClickListener {
      queryInput.setText("")
    }

    val robotoMono = ResourcesCompat.getFont(context, RobotoR.font.roboto_mono)
    queryInput.onTextChanged {
      queryClear.isVisible = it.isNotEmpty()
      queryInput.typeface = if (it.isEmpty()) Typeface.DEFAULT else robotoMono

      events.offer(Event.QueryChanged(it.toString()))
    }

    val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var totalDy = 0
    results.onScroll { _, dy ->
      if (dy > 0) {
        totalDy += dy
        if (totalDy >= touchSlop) {
          totalDy = 0

          val inputMethodManager = context.getSystemService<InputMethodManager>()!!
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

  override fun bind(model: Model, oldModel: Model?) {
    val count = model.count
    queryInput.hint = resources.getQuantityString(R.plurals.search_classes, count.toInt(), count)

    val queryResults = model.queryResults
    val itemResults = queryResults.items.map { ItemResult(queryResults.query, it) }
    resultsAdapter.submitList(itemResults) {
      // Always reset the scroll position to the top when the query changes.
      results.scrollToPosition(0)
    }

    if (model.syncStatus != IDLE) {
      val message = if (model.syncStatus == SYNC) R.string.updating else R.string.updating_failed

      var snackbar = this.snackbar
      if (snackbar == null) {
        snackbar = Snackbar.make(results, message, LENGTH_INDEFINITE)
        snackbar.show()
        this.snackbar = snackbar
      } else {
        snackbar.setText(message)
      }

      if (model.syncStatus == FAILED) {
        snackbar.setAction(R.string.dismiss) {
          events.offer(ClearSyncStatus)
        }
      }
    } else {
      snackbar?.dismiss()
    }
  }

  data class Args(val defaultQuery: String? = null)
}
