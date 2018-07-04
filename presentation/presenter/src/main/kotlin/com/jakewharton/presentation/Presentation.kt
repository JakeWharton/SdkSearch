package com.jakewharton.presentation

import kotlinx.coroutines.experimental.Job

fun Presenter<*, *>.startPresentation() = Presentation(this, start())

class Presentation internal constructor(
  val presenter: Presenter<*, *>,
  private val job: Job
) {
  fun stop() = job.cancel()
}
