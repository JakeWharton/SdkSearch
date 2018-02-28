package com.jakewharton.sdksearch.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.customtabs.CustomTabsIntent
import androidx.net.toUri
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.search.ui.ItemHandler
import com.jakewharton.sdksearch.store.Item

internal class OpenDocumentationItemHandler(
  private val context: Context,
  private val baseUrl: BaseUrl,
  private val androidReference: AndroidReference
) : ItemHandler {
  override fun invoke(item: Item) {
    val uri = baseUrl.resolve(item.link).toUri()
    val sourceUri = androidReference.sourceUrl(item.packageName, item.className)?.toUri()
    CustomTabsIntent.Builder()
        .setToolbarColor(context.getColor(R.color.green))
        .addDefaultShareMenuItem()
        .apply {
          if (sourceUri != null) {
            val sourceIntent = Intent(Intent.ACTION_VIEW, sourceUri)
            val pendingIntent = PendingIntent.getActivity(context, 123, sourceIntent, 0)
            setActionButton(
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_code_black_24dp),
                context.getString(R.string.view_class_source, item.className),
                pendingIntent,
                true)
          }
        }
        .build()
        .launchUrl(context, uri)
  }
}
