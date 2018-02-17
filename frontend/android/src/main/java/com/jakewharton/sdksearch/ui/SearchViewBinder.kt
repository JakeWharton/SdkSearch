package com.jakewharton.sdksearch.ui

import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
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
import com.jakewharton.sdksearch.util.layoutInflater
import com.jakewharton.sdksearch.util.onEditorAction
import com.jakewharton.sdksearch.util.onKey
import com.jakewharton.sdksearch.util.onScroll
import com.jakewharton.sdksearch.util.onTextChanged

class SearchViewBinder(
    view: View,
    onClick: ItemHandler,
    onCopy: ItemHandler,
    onShare: ItemHandler,
    onSource: ItemHandler
) {
  private val context = view.context

  // TODO all private + view model
  internal val results: RecyclerView = view.findViewById(R.id.results)
  internal val resultsAdapter = ItemAdapter(context.layoutInflater, onClick, onCopy, onShare, onSource)
  internal val queryInput: EditText = view.findViewById(R.id.query)
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
}
