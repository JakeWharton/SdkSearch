package com.squareup.sqldelight.runtime.coroutines

import com.squareup.sqldelight.Query
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.channels.map
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

fun <T : Any> Query<T>.asChannel(context: CoroutineContext): ReceiveChannel<Query<T>> {
  val channel = RendezvousChannel<Query<T>>()
  val listenerChannel = ListenerReceiveChannel(this, context, channel)

  addListener(listenerChannel)

  // Trigger initial emission.
  listenerChannel.queryResultsChanged()

  return listenerChannel
}

/** A single type which is both the query listener and receive channel delegate to save memory. */
private class ListenerReceiveChannel<T : Any>(
  private val query: Query<T>,
  private val context: CoroutineContext,
  private val channel: Channel<Query<T>>
) : Query.Listener, ReceiveChannel<Query<T>> by channel {
  override fun queryResultsChanged() {
    // TODO associate this job with the channel so that it gets canceled
    launch(context) {
      channel.send(query)
    }
  }

  override fun cancel(cause: Throwable?): Boolean {
    query.removeListener(this)
    return channel.cancel(cause)
  }
}

fun <T : Any> ReceiveChannel<Query<T>>.mapToOne() = map { it.executeAsOne() }

fun <T : Any> ReceiveChannel<Query<T>>.mapToList() = map { it.executeAsList() }

fun <T : Any> ReceiveChannel<Query<T>>.mapToOneOrNull() = map { it.executeAsOneOrNull() }
