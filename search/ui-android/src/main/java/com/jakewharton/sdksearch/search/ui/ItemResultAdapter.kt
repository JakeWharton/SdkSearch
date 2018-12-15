package com.jakewharton.sdksearch.search.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import com.jakewharton.sdksearch.store.item.Item

internal class ItemResultAdapter(
  private val inflater: LayoutInflater,
  private val callback: Callback
) : ListAdapter<ItemResult, ItemResultViewHolder>(ItemResultItemCallback) {

  fun invokeFirstItem() {
    currentList.firstOrNull()?.let { callback.onItemClicked(it.item) }
  }

  override fun onBindViewHolder(holder: ItemResultViewHolder, position: Int) {
    holder.update(currentList[position])
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemResultViewHolder {
    val view = inflater.inflate(R.layout.item, parent, false)
    return ItemResultViewHolder(view, callback)
  }

  interface Callback {
    fun onItemClicked(item: Item)
    fun onItemCopied(item: Item)
    fun onItemShared(item: Item)
    fun onItemViewSource(item: Item)
  }

  private object ItemResultItemCallback : ItemCallback<ItemResult>() {
    override fun areItemsTheSame(oldItem: ItemResult, newItem: ItemResult): Boolean {
      return oldItem.item.id == newItem.item.id
    }

    override fun areContentsTheSame(oldItem: ItemResult, newItem: ItemResult): Boolean {
      return oldItem == newItem
    }

    override fun getChangePayload(oldItem: ItemResult, newItem: ItemResult): Any? {
      if (oldItem == newItem) {
        return null
      }
      return Unit // Dummy value to prevent item change animation.
    }
  }
}
