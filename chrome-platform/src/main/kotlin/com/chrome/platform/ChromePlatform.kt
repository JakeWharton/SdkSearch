package com.chrome.platform

import com.chrome.platform.management.Management
import com.chrome.platform.omnibox.Omnibox
import com.chrome.platform.runtime.Runtime
import com.chrome.platform.storage.Storage
import com.chrome.platform.tabs.Tabs

/** https://developer.chrome.com/extensions/api_index */
external interface ChromePlatform {
  /** https://developer.chrome.com/extensions/management */
  val management: Management
  /** https://developer.chrome.com/extensions/omnibox */
  val omnibox: Omnibox
  /** https://developer.chrome.com/extensions/runtime */
  val runtime: Runtime
  /** https://developer.chrome.com/extensions/storage */
  val storage: Storage
  /** https://developer.chrome.com/extensions/tabs */
  val tabs: Tabs
}
