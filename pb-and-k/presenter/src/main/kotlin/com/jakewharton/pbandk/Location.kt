package com.jakewharton.pbandk

import kotlinx.coroutines.experimental.Job

fun Presenter<*, *>.startLocation(): Location {
  val job = start()
  return Location(this, job)
}

class Location internal constructor(
  val presenter: Presenter<*, *>,
  private val job: Job
) {
  fun stop() = job.cancel()
}
