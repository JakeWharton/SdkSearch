package com.chrome.platform

import com.chrome.platform.omnibox.Omnibox
import com.chrome.platform.tabs.Tabs

/** https://developer.chrome.com/extensions/api_index */
external interface ChromePlatform {
  /** https://developer.chrome.com/extensions/omnibox */
  val omnibox: Omnibox
  /** https://developer.chrome.com/extensions/tabs */
  val tabs: Tabs
}
