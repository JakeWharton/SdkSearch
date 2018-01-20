package com.jakewharton.sdksearch.store

expect interface ItemStore {
  suspend fun updateListing(listing: String, items: List<Item>)
}
