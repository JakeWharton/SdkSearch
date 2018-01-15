package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.util.toUri

internal class ClipboardCopyItemHandler(
  private val context: Context,
  private val baseUrl: BaseUrl
) : ItemHandler {
  override fun invoke(item: Item) {
    val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
    val uri = baseUrl.resolve(item.link()).toUri()
    clipboard.primaryClip = ClipData.newPlainText(item.class_(), uri.toString())
    val message = context.getString(R.string.copied, item.class_())
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }
}
