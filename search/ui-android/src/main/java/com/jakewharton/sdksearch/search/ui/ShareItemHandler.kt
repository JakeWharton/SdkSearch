package com.jakewharton.sdksearch.search.ui

import android.app.Activity
import androidx.core.app.ShareCompat.IntentBuilder
import com.jakewharton.sdksearch.store.item.Item

class ShareItemHandler(
  private val activity: Activity
) : ItemHandler {
  override fun invoke(item: Item) {
    IntentBuilder.from(activity)
        .setType("text/plain")
        .setChooserTitle(activity.getString(R.string.share_title, item.className))
        .setText(item.link)
        .startChooser()
  }
}
