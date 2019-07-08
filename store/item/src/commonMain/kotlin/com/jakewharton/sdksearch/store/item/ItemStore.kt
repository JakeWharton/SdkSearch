package com.jakewharton.sdksearch.store.item

import kotlinx.coroutines.flow.Flow

interface ItemStore {
  fun count(): Flow<Long>
  suspend fun updateItems(items: List<Item>)
  fun queryItems(term: String): Flow<List<Item>>
}
