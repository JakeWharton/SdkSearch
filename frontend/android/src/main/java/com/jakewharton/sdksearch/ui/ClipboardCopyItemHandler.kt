package com.jakewharton.sdksearch.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.systemService
import androidx.core.net.toUri
import androidx.core.widget.toast
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.search.ui.ItemHandler
import com.jakewharton.sdksearch.store.item.Item

internal class ClipboardCopyItemHandler(
  private val context: Context,
  private val baseUrl: BaseUrl
) : ItemHandler {
  override fun invoke(item: Item) {
    val clipboard = context.systemService<ClipboardManager>()
    val uri = baseUrl.resolve(item.link).toUri()
    clipboard.primaryClip = ClipData.newPlainText(item.className, uri.toString())
    context.toast(context.getString(R.string.copied, item.className))
  }
}
