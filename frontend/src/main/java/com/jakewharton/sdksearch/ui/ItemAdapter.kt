package com.jakewharton.sdksearch.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.db.Item

internal class ItemAdapter(
  private val inflater: LayoutInflater,
  private val onClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {

  private var items: List<Item> = emptyList()

  fun updateItems(items: List<Item>) {
    this.items = items
  }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    holder.setItem(items[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    val view = inflater.inflate(R.layout.item, parent, false) as TextView
    return ItemViewHolder(view, onClick)
  }

  override fun getItemCount() = items.size
}
