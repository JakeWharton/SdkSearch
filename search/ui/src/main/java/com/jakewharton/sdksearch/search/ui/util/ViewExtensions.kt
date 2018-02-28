package com.jakewharton.sdksearch.search.ui.util

import android.view.KeyEvent
import android.view.View

internal inline fun View.onKey(crossinline body: (KeyEvent) -> Boolean) {
  setOnKeyListener(object : View.OnKeyListener {
    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
      return body(event)
    }
  })
}
