package com.jakewharton.sdksearch.ui

import android.content.Context
import android.support.customtabs.CustomTabsIntent
import android.widget.Toast
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.util.toUri

internal class OpenSourceItemHandler(
  private val context: Context,
  private val androidReference: AndroidReference
) : ItemHandler {
  override fun invoke(item: Item) {
    val url = androidReference.sourceUrl(item.packageName(), item.className())
    if (url != null) {
      CustomTabsIntent.Builder()
          .setToolbarColor(context.getColor(R.color.green))
          .addDefaultShareMenuItem()
          .build()
          .launchUrl(context, url.toUri())
    } else {
      Toast.makeText(context, R.string.unknown_source, Toast.LENGTH_SHORT).show()
    }
  }
}
