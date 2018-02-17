package com.jakewharton.sdksearch.util

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.TextView

inline fun TextView.onTextChanged(crossinline body: (text: CharSequence) -> Unit) {
  addTextChangedListener(object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = body(s)
    override fun afterTextChanged(s: Editable?) = Unit
  })
}

inline fun TextView.onEditorAction(crossinline body: (actionId: Int) -> Boolean) {
  setOnEditorActionListener(object : TextView.OnEditorActionListener {
    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
      return body(actionId)
    }
  })
}
