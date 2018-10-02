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

  val listener = object : Query.Listener, (Throwable?) -> Unit {
    override fun queryResultsChanged() {
      channel.offer(this@asChannel)
    }

    override fun invoke(cause: Throwable?) {
      removeListener(this)
    }
  }

  addListener(listener)
  channel.invokeOnClose(listener)

  return channel
}

fun <T : Any> ReceiveChannel<Query<T>>.mapToOne(context: CoroutineContext) = map(context) { it.executeAsOne() }

fun <T : Any> ReceiveChannel<Query<T>>.mapToList(context: CoroutineContext) = map(context) { it.executeAsList() }

fun <T : Any> ReceiveChannel<Query<T>>.mapToOneOrNull(context: CoroutineContext) = map(context) { it.executeAsOneOrNull() }
