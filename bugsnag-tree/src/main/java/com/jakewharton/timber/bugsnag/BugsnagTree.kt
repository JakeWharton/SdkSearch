package com.jakewharton.timber.bugsnag

import android.util.Log.DEBUG
import android.util.Log.ERROR
import android.util.Log.INFO
import android.util.Log.VERBOSE
import android.util.Log.WARN
import com.bugsnag.android.Client
import com.bugsnag.android.Error
import com.bugsnag.android.Severity
import timber.log.Timber
import java.util.ArrayDeque
import java.util.Locale.US

/**
 * A logging implementation which buffers the last 200 messages and notifies on error exceptions.
 */
class BugsnagTree(private val client: Client) : Timber.Tree() {

  // Adding one to the initial size accounts for the add before remove.
  private val buffer = ArrayDeque<String>(BUFFER_SIZE + 1)

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (priority == VERBOSE || priority == DEBUG) {
      return
    }

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

    if (t != null) {
      when (priority) {
        ERROR -> client.notify(t, Severity.ERROR)
        WARN -> client.notify(t, Severity.WARNING)
        INFO -> client.notify(t, Severity.INFO)
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
