package com.jakewharton.sdksearch.ui

import android.support.v7.util.DiffUtil
import com.jakewharton.sdksearch.db.Item

internal class ItemDiffCallback(
  private val old: List<Item>,
  private val new: List<Item>
) : DiffUtil.Callback() {

  override fun getOldListSize() = old.size
  override fun getNewListSize() = new.size
  override fun areItemsTheSame(oldIndex: Int, newIndex: Int) = old[oldIndex]._id() == new[newIndex]._id()
  override fun areContentsTheSame(oldIndex: Int, newIndex: Int) = old[oldIndex] == new[newIndex]
}
