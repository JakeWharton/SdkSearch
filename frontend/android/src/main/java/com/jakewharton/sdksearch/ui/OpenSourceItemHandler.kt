package com.jakewharton.sdksearch.ui

import android.content.Context
import android.support.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.widget.toast
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.search.ui.ItemHandler

internal class OpenSourceItemHandler(
  private val context: Context,
  private val androidReference: AndroidReference
) : ItemHandler {
  override fun invoke(item: Item) {
    val url = androidReference.sourceUrl(item.packageName, item.className)
    if (url != null) {
      CustomTabsIntent.Builder()
          .setToolbarColor(context.getColor(R.color.green))
          .addDefaultShareMenuItem()
          .build()
          .launchUrl(context, url.toUri())
    } else {
      context.toast(R.string.unknown_source)
    }
  }
}
