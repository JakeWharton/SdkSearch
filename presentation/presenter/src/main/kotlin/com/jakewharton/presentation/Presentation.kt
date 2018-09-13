package com.jakewharton.presentation

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

fun Presenter<*, *>.startPresentation(dispatcher: CoroutineDispatcher): Presentation {
  val job = GlobalScope.launch(dispatcher) { start() }
  return Presentation(this, job)
}

class Presentation internal constructor(
  val presenter: Presenter<*, *>,
  private val job: Job
) {
  fun stop() = job.cancel()
}
