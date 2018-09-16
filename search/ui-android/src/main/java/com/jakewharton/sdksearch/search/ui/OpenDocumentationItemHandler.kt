package com.jakewharton.sdksearch.search.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.store.item.Item
import okhttp3.HttpUrl

class OpenDocumentationItemHandler(
  private val context: Context,
  private val baseUrl: HttpUrl,
  private val androidReference: AndroidReference
) : ItemHandler {
  override fun invoke(item: Item) {
    val uri = baseUrl.resolve(item.link)!!.toString().toUri()
    val sourceUri = androidReference.sourceUrl(item.packageName, item.className)?.toUri()

    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
    val colorPrimary = typedValue.data

    CustomTabsIntent.Builder()
        .setToolbarColor(colorPrimary)
        .addDefaultShareMenuItem()
        .apply {
          if (sourceUri != null) {
            val sourceIntent = Intent(Intent.ACTION_VIEW, sourceUri)
            val pendingIntent = PendingIntent.getActivity(context, 123, sourceIntent, 0)
            setActionButton(
                context.getDrawable(R.drawable.ic_code_black_24dp)!!.toBitmap(),
                context.getString(R.string.view_class_source, item.className),
                pendingIntent,
                true)
          }
        }
        .build()
        .launchUrl(context, uri)
  }
}
