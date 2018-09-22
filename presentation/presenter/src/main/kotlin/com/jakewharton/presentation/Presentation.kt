package com.jakewharton.presentation

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
