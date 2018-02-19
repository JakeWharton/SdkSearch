package com.jakewharton.sdksearch.ui

import android.support.v7.util.DiffUtil
import com.jakewharton.sdksearch.ui.SearchViewBinder.Model.QueryResults

internal class ItemDiffer(
  oldResults: QueryResults,
  newResults: QueryResults
) : DiffUtil.Callback() {
  private val oldQuery = oldResults.query
  private val old = oldResults.items
  private val newQuery = newResults.query
  private val new = newResults.items

  private val queriesTheSame = oldQuery == newQuery

  override fun getOldListSize() = old.size
  override fun getNewListSize() = new.size

  override fun areItemsTheSame(oldIndex: Int, newIndex: Int): Boolean {
    return old[oldIndex].id == new[newIndex].id
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
