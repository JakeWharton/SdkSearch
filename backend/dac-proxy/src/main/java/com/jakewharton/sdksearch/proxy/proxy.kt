@file:JvmName("Proxy")

package com.jakewharton.sdksearch.proxy

import com.jakewharton.sdksearch.proxy.model.DocumentedType
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.DefaultHeaders
import io.ktor.features.gzip
import io.ktor.http.ContentType.Application
import io.ktor.response.header
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.slf4j.event.Level
import kotlin.time.minutes

fun main() {
  val port = System.getenv("PORT")?.toIntOrNull() ?: 8084
  embeddedServer(Netty, port) {
    install(DefaultHeaders)
    install(CallLogging) {
      level = Level.INFO
    }
    install(Compression) {
      gzip()
    }
    install(Routing) {
      get("/list") {
        call.response.header("Access-Control-Allow-Origin", "https://sdksearch.app")
        call.respondText(jsonCache(), Application.Json)
      }
    }
  }.start()
}

private val jsonCache = ::listToJson.memoize(expiration = 30.minutes)

private suspend fun listToJson(): String {
  return Json.stringify(DocumentedType.serializer().list, listDocumentedTypes())
}
