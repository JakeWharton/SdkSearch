package com.jakewharton.sdksearch.store

import io.reactivex.Observable

actual interface ItemStore {
  fun count(): Observable<Long>
  actual fun updateListing(listing: String, items: List<Item>)
  fun queryItems(term: String): Observable<List<Item>>
}
