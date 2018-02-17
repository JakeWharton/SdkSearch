package com.jakewharton.sdksearch.util

import android.support.v7.widget.RecyclerView

inline fun RecyclerView.onScroll(crossinline body: (dx: Int, dy: Int) -> Unit) {
  addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      body(dx, dy)
    }
  })
}
