package com.jakewharton.sdksearch.store

actual interface ItemStore {
  actual suspend fun updateListing(listing: String, items: List<Item>)
}
