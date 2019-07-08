package com.jakewharton.sdksearch.search.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.getSystemService
import com.jakewharton.sdksearch.store.item.Item

class ClipboardCopyItemHandler(
  private val context: Context
) : ItemHandler {
  override fun invoke(item: Item) {
    val clipboard = context.getSystemService<ClipboardManager>()!!
    clipboard.setPrimaryClip(ClipData.newPlainText(item.className, item.link))
    Toast.makeText(context, context.getString(R.string.copied, item.className), LENGTH_SHORT).show()
  }
}
