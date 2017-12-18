package com.jakewharton.sdksearch.util

import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.Callback
import android.support.v7.util.DiffUtil.DiffResult

data class DataWithDiff<out T>(val data: T, val diff: DiffResult = EMPTY_RESULT)

private val EMPTY_RESULT = DiffUtil.calculateDiff(object : Callback() {
  override fun getOldListSize() = 0
  override fun getNewListSize() = 0
  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = throw AssertionError()
  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = throw AssertionError()
})
