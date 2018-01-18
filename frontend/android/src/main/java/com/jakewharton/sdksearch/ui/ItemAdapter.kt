package com.jakewharton.sdksearch.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.store.Item

internal class ItemAdapter(
  private val inflater: LayoutInflater,
  private val onClick: (Item) -> Unit,
  private val onCopy: (Item) -> Unit,
  private val onShare: (Item) -> Unit,
  private val onSource: (Item) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {
  private var query = ""
  private var items: List<Item> = emptyList()

  fun invokeFirstItem() {
    items.firstOrNull()?.let(onClick)
  }

  fun updateItems(query: String, items: List<Item>) {
    this.query = query
    this.items = items
  }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    holder.setItem(query, items[position])
  }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int, payloads: List<Any>) {
    if (payloads.isEmpty()) {
      onBindViewHolder(holder, position)
      return
    }

    for (payload in payloads) {
      when (payload) {
        QUERY_CHANGED -> holder.updateQuery(query)
        ITEM_CHANGED -> holder.updateItem(items[position])
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    val view = inflater.inflate(R.layout.item, parent, false)
    return ItemViewHolder(view, onClick, onCopy, onShare, onSource)
  }

  override fun getItemCount() = items.size

  companion object {
    val QUERY_CHANGED = Any()
    val ITEM_CHANGED = Any()
  }
}
