package com.jakewharton.sdksearch.ui

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import com.jakewharton.sdksearch.db.Item

internal class ItemViewHolder(
  private val view: TextView,
  private val onClick: (Item) -> Unit
) : ViewHolder(view), OnClickListener {
  init {
    view.setOnClickListener(this)
  }

  private var item: Item? = null

  override fun onClick(view: View) {
    onClick.invoke(item!!)
  }

  fun setItem(item: Item) {
    this.item = item

    view.text = item.label()
  }
}
