@file:JvmName("ReferenceValidator")

package com.jakewharton.sdksearch.reference.validator

import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.AndroidReference
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.minutes

private class CliConfig(parser: ArgParser) {
  val packages by parser.positionalList("PACKAGE", help = "package prefixes to validate (default: all)")
      .default(listOf(""))
}

suspend fun main(vararg args: String) {
  val config = mainBody {
    val parser = ArgParser(args)
    val config = CliConfig(parser)
    parser.force()
    return@mainBody config
  }

  val client = OkHttpClient.Builder()
      .readTimeout(1.minutes)
      .build()
  val service = DacComponent.create(client)
      .documentationService()

  val fqcns = service.list()
      .asSequence()
      .filterNot { it.className == "R" || it.className.startsWith("R.") }
      .filter { item -> config.packages.any { item.packageName.startsWith(it) } }
      .sortedWith(compareBy({ it.packageName }, { it.className }))
      .toList()

  var pad = 0
  fun logStatus(message: String) {
    print("\r$message".padEnd(pad))
    pad = message.length + 1 // Account for \r
  }
  fun logPrint(message: String) {
    println("\r$message".padEnd(pad))
    pad = 0
  }

  var index = 0
  while (index < fqcns.size) {
    val percentage = (index.toFloat() / fqcns.size * 1000).toInt() / 10.0
    val checking = "Checking ${index + 1} of ${fqcns.size} ($percentage%)…"
    logStatus(checking)

    val fqcn = fqcns[index]
    val url = AndroidReference.sourceUrl(fqcn.packageName, fqcn.className)
    if (url != null) {
      val request = Builder().head().url(url).build()
      client.newCall(request).await().use {
        val code = it.code
        if (code == 429) {
          logStatus("$checking Rate limited! Cooling off…")
          delay(2.minutes)
          index -= 1 // Try this index again.
        } else if (!it.isSuccessful) {
          logPrint("$code $fqcn $url")
        }
      }
    }
    index++
  }

  logPrint("Checked $index references!")

  // Shut down OkHttpClient resources so that the JVM can exit cleanly.
  client.dispatcher.executorService.shutdown()
  client.connectionPool.evictAll()
}

// TODO https://github.com/Kotlin/kotlinx.coroutines/issues/1402
private suspend fun delay(duration: Duration) {
  kotlinx.coroutines.delay(duration.toLongMilliseconds())
}

// TODO https://github.com/square/okhttp/issues/5322
private fun OkHttpClient.Builder.readTimeout(duration: Duration) = apply {
  readTimeout(duration.toLongMilliseconds(), TimeUnit.MILLISECONDS)
}
