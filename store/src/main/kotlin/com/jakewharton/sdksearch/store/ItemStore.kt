package com.jakewharton.sdksearch.store

expect interface ItemStore {
  suspend fun updateItems(items: List<Item>)
}
