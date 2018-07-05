package com.jakewharton.sdksearch.search.ui

import android.content.Context
import android.support.customtabs.CustomTabsIntent
import android.util.TypedValue
import androidx.core.net.toUri
import androidx.core.widget.toast
import com.jakewharton.sdksearch.store.item.Item
import com.jakewharton.sdksearch.reference.AndroidReference

class OpenSourceItemHandler(
  private val context: Context,
  private val androidReference: AndroidReference
) : ItemHandler {
  override fun invoke(item: Item) {
    val url = androidReference.sourceUrl(item.packageName, item.className)
    if (url != null) {
      val typedValue = TypedValue()
      context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
      val colorPrimary = typedValue.data

      CustomTabsIntent.Builder()
          .setToolbarColor(colorPrimary)
          .addDefaultShareMenuItem()
          .build()
          .launchUrl(context, url.toUri())
    } else {
      context.toast(R.string.unknown_source)
    }
  }
}
