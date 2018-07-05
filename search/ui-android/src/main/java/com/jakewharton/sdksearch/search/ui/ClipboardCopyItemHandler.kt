package com.jakewharton.sdksearch.search.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.systemService
import androidx.core.widget.toast
import com.jakewharton.sdksearch.store.item.Item
import okhttp3.HttpUrl

class ClipboardCopyItemHandler(
  private val context: Context,
  private val baseUrl: HttpUrl
) : ItemHandler {
  override fun invoke(item: Item) {
    val clipboard = context.systemService<ClipboardManager>()
    val uri = baseUrl.resolve(item.link)!!
    clipboard.primaryClip = ClipData.newPlainText(item.className, uri.toString())
    context.toast(context.getString(R.string.copied, item.className))
  }
}
