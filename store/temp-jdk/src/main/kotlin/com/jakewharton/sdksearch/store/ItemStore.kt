package com.jakewharton.sdksearch.store

import io.reactivex.Observable

actual interface ItemStore {
  fun count(): Observable<Long>
  actual suspend fun updateItems(items: List<Item>)
  fun queryItems(term: String): Observable<List<Item>>
}
