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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration

fun <R> memoizeWithExpiration(
  duration: Duration = Duration.ZERO,
  nanoSupplier: () -> Long = System::nanoTime,
  supplier: suspend () -> R
): MemoizedSuspendingSupplier<R> {
  require(!duration.isNegative) { "Duration must be positive: $duration" }
  return MemoizedSuspendingSupplier(duration, nanoSupplier, supplier)
}

class MemoizedSuspendingSupplier<R> internal constructor(
  duration: Duration,
  private val nanoSupplier: () -> Long,
  private val delegate: suspend () -> R
) {
  private val durationNanos = duration.toNanos() // TODO handle saturation
  private val lock = Mutex()
  @Volatile private var value: Any? = null
  @Volatile private var expirationNanos = 0L // The special value 0 means "not yet initialized".

  suspend operator fun invoke(): R {
    var nanos = expirationNanos
    val now = nanoSupplier()
    if (nanos == 0L || now - nanos >= 0L) {
      lock.withLock {
        // Recheck for lost race.
        if (nanos == expirationNanos) {
          val newValue = delegate()
          value = newValue

          nanos = now + durationNanos
          // In the unlikely event nanos is 0, set it to 1; no one will notice 1 ns of tardiness.
          expirationNanos = if (nanos == 0L) 1L else nanos

          return newValue
        }
      }
    }
    @Suppress("UNCHECKED_CAST")
    return value as R
  }
}
