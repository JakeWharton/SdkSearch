package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.support.v4.app.ShareCompat.IntentBuilder
import androidx.core.net.toUri
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.search.ui.ItemHandler
import com.jakewharton.sdksearch.store.item.Item

internal class ShareItemHandler(
  private val activity: Activity,
  private val baseUrl: BaseUrl
) : ItemHandler {
  override fun invoke(item: Item) {
    val uri = baseUrl.resolve(item.link).toUri()
    IntentBuilder.from(activity)
        .setType("text/plain")
        .setChooserTitle(activity.getString(R.string.share_title, item.className))
        .setText(uri.toString())
        .startChooser()
  }
}
