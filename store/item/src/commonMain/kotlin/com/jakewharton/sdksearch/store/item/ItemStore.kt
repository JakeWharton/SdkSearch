package com.jakewharton.sdksearch.store.item

import kotlinx.coroutines.channels.ReceiveChannel

interface ItemStore {
  fun count(): ReceiveChannel<Long>
  suspend fun updateItems(items: List<Item>)
  fun queryItems(term: String): ReceiveChannel<List<Item>>
}
