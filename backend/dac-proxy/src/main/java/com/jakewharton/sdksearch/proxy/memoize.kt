/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Adapted from Guava's Suppliers.java

package com.jakewharton.sdksearch.proxy

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun <R> (suspend () -> R).memoize(
  expiration: Duration = Duration.INFINITE,
  clock: TimeSource = Monotonic
): suspend () -> R {
  require(!expiration.isNegative()) { "Duration must be positive: $expiration" }

  // TODO remove ::invoke once https://youtrack.jetbrains.com/issue/KT-18707 ships
  return MemoizedSuspendingSupplier(expiration, clock, this)::invoke
}

private class MemoizedSuspendingSupplier<R>(
  private val expiration: Duration,
  clock: TimeSource,
  private val delegate: suspend () -> R
) {
  private val lock = Mutex()
  @Volatile private var value: Any? = null

  /**
   * A [TimeMark] for which an elapsed time of 0 or greater means [value] has expired and should
   * be (re)obtained from [delegate].
   */
  @Volatile private var expirationMark = clock.markNow()

  suspend operator fun invoke(): R {
    val mark = expirationMark
    if (mark.hasPassedNow()) {
      lock.withLock {
        // Recheck for lost race.
        if (mark === expirationMark) {
          val newValue = delegate()
          value = newValue
          expirationMark = mark + expiration

          return newValue
        }
      }
    }
    @Suppress("UNCHECKED_CAST")
    return value as R
  }
}
