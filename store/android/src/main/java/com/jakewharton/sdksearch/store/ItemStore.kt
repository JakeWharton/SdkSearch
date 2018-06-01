package com.jakewharton.sdksearch.store

import kotlinx.coroutines.experimental.channels.ReceiveChannel

actual interface ItemStore {
  fun count(): ReceiveChannel<Long>
  actual suspend fun updateItems(items: List<Item>)
  fun queryItems(term: String): ReceiveChannel<List<Item>>
}
