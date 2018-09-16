package com.jakewharton.sdksearch.search.ui

import android.app.Activity
import androidx.core.app.ShareCompat.IntentBuilder
import com.jakewharton.sdksearch.store.item.Item
import okhttp3.HttpUrl

class ShareItemHandler(
  private val activity: Activity,
  private val baseUrl: HttpUrl
) : ItemHandler {
  override fun invoke(item: Item) {
    val uri = baseUrl.resolve(item.link)!!
    IntentBuilder.from(activity)
        .setType("text/plain")
        .setChooserTitle(activity.getString(R.string.share_title, item.className))
        .setText(uri.toString())
        .startChooser()
  }
}
