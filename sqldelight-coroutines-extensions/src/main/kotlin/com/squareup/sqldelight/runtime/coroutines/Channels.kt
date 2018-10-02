package com.squareup.sqldelight.runtime.coroutines

import com.squareup.sqldelight.Query
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlin.coroutines.CoroutineContext

fun <T : Any> Query<T>.asChannel(): ReceiveChannel<Query<T>> {
  val channel = Channel<Query<T>>(CONFLATED)
  // Ensure consumers immediately run the query.
  channel.offer(this)

  val listenerChannel = ListenerReceiveChannel(this, channel)
  addListener(listenerChannel)

  return listenerChannel
}

/** A single type which is both the query listener and receive channel delegate to save memory. */
private class ListenerReceiveChannel<T : Any>(
  private val query: Query<T>,
  private val channel: Channel<Query<T>>
) : Query.Listener, ReceiveChannel<Query<T>> by channel {
  override fun queryResultsChanged() {
    channel.offer(query)
  }

  override fun cancel(cause: Throwable?): Boolean {
    query.removeListener(this)
    return channel.cancel(cause)
  }
}

fun <T : Any> ReceiveChannel<Query<T>>.mapToOne(context: CoroutineContext) = map(context) { it.executeAsOne() }

fun <T : Any> ReceiveChannel<Query<T>>.mapToList(context: CoroutineContext) = map(context) { it.executeAsList() }

fun <T : Any> ReceiveChannel<Query<T>>.mapToOneOrNull(context: CoroutineContext) = map(context) { it.executeAsOneOrNull() }
