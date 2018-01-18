package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.support.v4.app.ShareCompat.IntentBuilder
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.api.dac.BaseUrl
import com.jakewharton.sdksearch.store.Item
import com.jakewharton.sdksearch.util.toUri

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
