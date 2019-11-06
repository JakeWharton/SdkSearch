package com.jakewharton.timber.bugsnag

import android.util.Log.DEBUG
import android.util.Log.ERROR
import android.util.Log.INFO
import android.util.Log.VERBOSE
import android.util.Log.WARN
import com.bugsnag.android.Client
import com.bugsnag.android.Error
import com.bugsnag.android.Severity
import java.util.ArrayDeque
import java.util.Locale.US
import timber.log.Tree

/**
 * A logging implementation which buffers the last 200 messages and notifies on error exceptions.
 */
class BugsnagTree(private val client: Client) : Tree() {

  // Adding one to the initial size accounts for the add before remove.
  private val buffer = ArrayDeque<String>(BUFFER_SIZE + 1)

  override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
    if (priority == VERBOSE || priority == DEBUG) {
      return
    }

    if (message != null) {
      val log = buildString(message.length + 16) {
        append(System.currentTimeMillis())
        append(' ')
        append(when (priority) {
          ERROR -> "E"
          WARN -> "W"
          INFO -> "I"
          else -> priority.toString()
        })
        append(' ')
        append(message)
      }
      // TODO replace with lock free?
      synchronized(buffer) {
        buffer.addLast(log)
        if (buffer.size > BUFFER_SIZE) {
          buffer.removeFirst()
        }
      }
    }

    if (throwable != null) {
      when (priority) {
        ERROR -> client.notify(throwable, Severity.ERROR)
        WARN -> client.notify(throwable, Severity.WARNING)
        INFO -> client.notify(throwable, Severity.INFO)
      }
    }
  }

  fun update(error: Error) {
    synchronized(buffer) {
      buffer.forEachIndexed { index, message ->
        error.addToTab("Log", String.format(US, "%03d", index), message)
      }
    }
  }

  companion object {
    private const val BUFFER_SIZE = 200
  }
}
