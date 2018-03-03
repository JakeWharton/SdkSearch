package com.jakewharton.sdksearch.ui

import android.content.Context
import android.support.customtabs.CustomTabsIntent
import com.jakewharton.sdksearch.R

internal class CustomTabsHelper {
  companion object {
    fun intent(context: Context): CustomTabsIntent = builder(context).build()
    fun builder(context: Context): CustomTabsIntent.Builder = CustomTabsIntent.Builder()
            .setToolbarColor(context.getColor(R.color.green))
            .addDefaultShareMenuItem()
  }
}
