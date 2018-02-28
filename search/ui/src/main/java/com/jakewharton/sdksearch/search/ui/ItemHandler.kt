package com.jakewharton.sdksearch.search.ui

import com.jakewharton.sdksearch.store.Item

interface ItemHandler {
  operator fun invoke(item: Item)
}
