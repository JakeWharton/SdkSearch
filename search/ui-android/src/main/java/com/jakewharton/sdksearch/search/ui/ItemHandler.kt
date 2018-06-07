package com.jakewharton.sdksearch.search.ui

import com.jakewharton.sdksearch.store.item.Item

interface ItemHandler {
  operator fun invoke(item: Item)
}
