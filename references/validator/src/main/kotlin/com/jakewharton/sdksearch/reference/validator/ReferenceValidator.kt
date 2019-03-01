@file:JvmName("ReferenceValidator")

package com.jakewharton.sdksearch.reference.validator

import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.AndroidReference
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import kotlinx.coroutines.time.delay
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import java.time.Duration

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
      .readTimeout(Duration.ofMinutes(1))
      .build()
  val service = DacComponent.builder()
      .client(client)
      .build()
      .documentationService()

  val fqcns = service.list().await()
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
        val code = it.code()
        if (code == 429) {
          logStatus("$checking Rate limited! Cooling off…")
          delay(Duration.ofMinutes(2))
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
  client.dispatcher().executorService().shutdown()
  client.connectionPool().evictAll()
}
