package com.jakewharton.sdksearch.db

import io.reactivex.Observable

interface ItemStore {
  fun count(): Observable<Long>
  fun updateListing(listing: String, items: List<Item>)
  fun queryItems(term: String): Observable<List<Item>>
}
