package com.chrome.platform.storage

/** https://developer.chrome.com/apps/storage#type-StorageChange */
external interface StorageChange {
  val oldValue: Any?
  val newValue: Any?
}
