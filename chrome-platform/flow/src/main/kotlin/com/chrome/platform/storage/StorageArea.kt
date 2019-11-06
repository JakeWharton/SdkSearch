package com.chrome.platform.storage

import kotlin.js.Json
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

fun StorageArea.changes(key: String): Flow<Any?> = callbackFlow {
  send(get(key))

  val listener: (Json, String) -> Unit = { objects, _ ->
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val change = objects[key] as StorageChange?
    if (change != null) {
      offer(change.newValue)
    }
  }
  onChanged.addListener(listener)
  awaitClose {
    onChanged.removeListener(listener)
  }
}.conflate()
