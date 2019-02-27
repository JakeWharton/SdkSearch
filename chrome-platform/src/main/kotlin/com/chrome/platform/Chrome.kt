package com.chrome.platform

import com.chrome.platform.omnibox.Omnibox
import com.chrome.platform.runtime.Runtime
import com.chrome.platform.storage.Storage
import com.chrome.platform.tabs.Tabs

/** Global instance of the [ChromePlatform]. */
@JsName("chrome")
external object Chrome : ChromePlatform {
  override val omnibox: Omnibox
  override val runtime: Runtime
  override val storage: Storage
  override val tabs: Tabs
}
