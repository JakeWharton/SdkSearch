package com.jakewharton.presentation

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel

interface Presenter<ModelT : Any, EventT : Any> {
  val models: ReceiveChannel<ModelT>
  val events: SendChannel<EventT>
  fun start(): Job
}
