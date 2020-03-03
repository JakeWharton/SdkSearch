package com.jakewharton.sdksearch.proxy

import com.google.common.truth.Truth.assertThat
import kotlin.time.TestTimeSource
import kotlin.time.minutes
import kotlin.time.nanoseconds
import kotlin.time.seconds
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Test

class MemoizeTest {
  @Test fun negativeExpirationDisallowed() {
    val negativeExpiration = (-1).minutes

    assertThrows<IllegalArgumentException> {
      suspend {}.memoize(negativeExpiration)
    }.hasMessageThat().isEqualTo("Duration must be positive: $negativeExpiration")
  }

  @Test fun supplierInvokedImmediately() = runBlocking {
    var calls = 0

    val supplier = suspend {
      ++calls
    }.memoize()

    assertThat(calls).isEqualTo(0)
    assertThat(supplier.invoke()).isEqualTo(1)
    assertThat(supplier.invoke()).isEqualTo(1)
  }

  @Test fun defaultExpirationIsInfinity() = runBlocking {
    val clock = TestTimeSource()
    var calls = 0

    val supplier = suspend {
      ++calls
    }.memoize(clock = clock)

    assertThat(supplier.invoke()).isEqualTo(1)

    clock += Long.MAX_VALUE.nanoseconds
    assertThat(supplier.invoke()).isEqualTo(1)
  }

  @Test fun throwingSupplierDoesNotResetExpiration() = runBlocking {
    var calls = 0
    val failureException = RuntimeException()

    val supplier = suspend {
      if (calls++ == 0) {
        throw failureException
      }
      calls
    }.memoize()

    assertThrows<RuntimeException> {
      supplier.invoke()
    }.isSameInstanceAs(failureException)

    assertThat(supplier.invoke()).isEqualTo(2)
  }

  @Test fun supplerInvokedAgainAfterExpiration() = runBlocking {
    val clock = TestTimeSource()
    var calls = 0

    val supplier = suspend {
      ++calls
    }.memoize(10.seconds, clock)

    assertThat(supplier.invoke()).isEqualTo(1)

    clock += 9.seconds
    assertThat(supplier.invoke()).isEqualTo(1)

    clock += 1.seconds
    assertThat(supplier.invoke()).isEqualTo(2)
  }

  @Test fun supplerExecutionTimeIsCountedAgainstExpiration() = runBlocking {
    val clock = TestTimeSource()
    var calls = 0

    val supplier = suspend {
      // Take longer to execute than the expiration.
      clock += 11.seconds

      ++calls
    }.memoize(10.seconds, clock)

    assertThat(supplier.invoke()).isEqualTo(1)
    assertThat(supplier.invoke()).isEqualTo(2)
  }

  @Test fun racingThreadsInvokeSupplierOnce() = runBlocking {
    var calls = 0
    val completionSignal = Job()
    var suspendedPendingCompletion = false

    val supplier = suspend {
      if (calls == 0) {
        // Suspend first execution of this supplier on an external completion signal.
        suspendedPendingCompletion = true
        completionSignal.join()
      }

      ++calls
    }.memoize()

    // Eagerly start a coroutine which triggers the supplier and causes it to suspend at the latch.
    val invokingChild = async(start = UNDISPATCHED) {
      supplier.invoke()
    }
    assertThat(suspendedPendingCompletion).isTrue()

    // Start a second coroutine which wants to invoke the supplier but should not be able to.
    val awaitingChild = async {
      supplier.invoke()
    }

    // Allow the first supplier invocation to finish. This will unblock the second coroutine which
    // should determine it has no work to do and return the value from the first call.
    completionSignal.complete()
    assertThat(invokingChild.await()).isEqualTo(1)
    assertThat(awaitingChild.await()).isEqualTo(1)
  }
}
