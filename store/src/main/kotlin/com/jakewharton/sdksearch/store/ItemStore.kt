package com.jakewharton.sdksearch.store

expect interface ItemStore {
  fun updateListing(listing: String, items: List<Item>)
}
