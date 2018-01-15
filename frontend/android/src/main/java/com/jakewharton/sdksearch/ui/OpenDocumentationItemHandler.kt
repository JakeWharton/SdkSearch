package com.jakewharton.sdksearch.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.customtabs.CustomTabsIntent
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.db.Item
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.util.toUri

internal class OpenDocumentationItemHandler(
  private val context: Context,
  private val baseUrl: BaseUrl,
  private val androidReference: AndroidReference
) : ItemHandler {
  override fun invoke(item: Item) {
    val uri = baseUrl.resolve(item.link()).toUri()
    val sourceUri = androidReference.sourceUrl(item.package_(), item.class_())?.toUri()
    CustomTabsIntent.Builder()
        .setToolbarColor(context.getColor(R.color.green))
        .addDefaultShareMenuItem()
        .apply {
          if (sourceUri != null) {
            val sourceIntent = Intent(Intent.ACTION_VIEW, sourceUri)
            val pendingIntent = PendingIntent.getActivity(context, 123, sourceIntent, 0)
            addMenuItem(context.getString(R.string.view_class_source, item.class_()), pendingIntent)
          }
        }
        .build()
        .launchUrl( context, uri)
  }
}
