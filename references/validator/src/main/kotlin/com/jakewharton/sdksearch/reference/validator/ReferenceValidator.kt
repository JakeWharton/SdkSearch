@file:JvmName("ReferenceValidator")

package com.jakewharton.sdksearch.reference.validator

import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.reference.AndroidReference
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import java.util.concurrent.TimeUnit.MINUTES

private val FQCN_PACKAGE = "^([a-z0-9]+\\.)+".toRegex()
private val FQCN = "\\.[A-Z]".toRegex()

private class CliConfig(parser: ArgParser) {
  val gitWeb by parser.storing("--git-web", argName = "HOST", help = "Git web host (default: $PRODUCTION_GIT_WEB)")
      .default(PRODUCTION_GIT_WEB)

  val dac by parser.storing("--dac", argName = "HOST", help = "DAC host (default: $PRODUCTION_DAC)")
      .default(PRODUCTION_DAC)

  val packages by parser.positionalList("PACKAGE", help = "package prefixes to validate (default: all)")
      .default(listOf(""))
}

fun main(vararg args: String) = runBlocking {
  val config = mainBody {
    val parser = ArgParser(args)
    val config = CliConfig(parser)
    parser.force()
    return@mainBody config
  }

  val client = OkHttpClient()
  val service = DacComponent.builder()
      .baseUrl(HttpUrl.get(config.dac))
      .client(client)
      .build()
      .documentationService()

  val fqcns = service.list().await().values.single()
      .asSequence()
      .map { it.type }
      .filter { FQCN.find(it) != null }
      .filterNot { it.contains(".R.") || it.endsWith(".R") }
      .filter { fqcn -> config.packages.any { fqcn.startsWith(it) } }
      .sorted()
      .toList()

  val reference = AndroidReference(config.gitWeb, config.dac)

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
    val matcher = checkNotNull(FQCN_PACKAGE.find(fqcn)) { "FQCN '$fqcn' is not valid." }
    val packageName = fqcn.substring(0, matcher.range.endInclusive)
    val className = fqcn.substring(matcher.range.endInclusive + 1)

    val url = reference.sourceUrl(packageName, className)
    if (url != null) {
      val request = Builder().head().url(url).build()
      client.newCall(request).execute().use {
        val code = it.code()
        if (code == 429) {
          logStatus("$checking Rate limited! Cooling off…")
          delay(2, MINUTES)
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
