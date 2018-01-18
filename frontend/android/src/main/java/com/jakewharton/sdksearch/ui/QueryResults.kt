package com.jakewharton.sdksearch.ui

import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.Callback
import android.support.v7.util.DiffUtil.DiffResult
import com.jakewharton.sdksearch.store.Item

internal data class QueryResults(
  val query: String = "",
  val data: List<Item> = emptyList(),
  val diff: DiffResult = EMPTY_RESULT
)

private val EMPTY_RESULT = DiffUtil.calculateDiff(object : Callback() {
  override fun getOldListSize() = 0
  override fun getNewListSize() = 0
  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = throw AssertionError()
  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = throw AssertionError()
})
