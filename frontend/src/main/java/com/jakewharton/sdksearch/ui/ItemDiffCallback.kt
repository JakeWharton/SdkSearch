package com.jakewharton.sdksearch.ui

import android.support.v7.util.DiffUtil
import com.jakewharton.sdksearch.db.Item

internal class ItemDiffCallback(
  oldQuery: String,
  private val old: List<Item>,
  newQuery: String,
  private val new: List<Item>
) : DiffUtil.Callback() {

  private val queriesTheSame = oldQuery == newQuery

  override fun getOldListSize() = old.size
  override fun getNewListSize() = new.size

  override fun areItemsTheSame(oldIndex: Int, newIndex: Int): Boolean {
    return old[oldIndex]._id() == new[newIndex]._id()
  }

  override fun areContentsTheSame(oldIndex: Int, newIndex: Int): Boolean {
    return queriesTheSame && old[oldIndex] == new[newIndex]
  }

  override fun getChangePayload(oldIndex: Int, newIndex: Int): Any? {
    if (queriesTheSame && old[oldIndex] == new[newIndex]) {
      return null
    }
    if (queriesTheSame) {
      return ItemAdapter.ITEM_CHANGED
    }
    return ItemAdapter.QUERY_CHANGED
  }
}
