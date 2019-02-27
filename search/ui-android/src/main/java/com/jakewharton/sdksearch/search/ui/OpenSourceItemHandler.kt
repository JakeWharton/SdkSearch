package com.jakewharton.sdksearch.search.ui

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.store.item.Item

class OpenSourceItemHandler(private val context: Context) : ItemHandler {
  override fun invoke(item: Item) {
    val url = AndroidReference.sourceUrl(item.packageName, item.className)
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
      Toast.makeText(context, R.string.unknown_source, LENGTH_SHORT).show()
    }
  }
}
