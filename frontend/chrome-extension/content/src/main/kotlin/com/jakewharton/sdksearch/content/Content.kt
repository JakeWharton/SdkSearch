package com.jakewharton.sdksearch.content

import com.chrome.platform.Chrome
import com.jakewharton.sdksearch.sourcelinker.insertSourceLink
import timber.log.ConsoleTree
import timber.log.Timber
import kotlin.browser.document
import kotlin.browser.window

suspend fun main() {
  Timber.plant(ConsoleTree())

  val isDebug = "Debug" in Chrome.runtime.manifest["name"].unsafeCast<String>()
  document.insertSourceLink(window.location.href, isDebug)
}
