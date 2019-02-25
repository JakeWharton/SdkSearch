@file:JvmName("Proxy")

package com.jakewharton.sdksearch.proxy

import com.jakewharton.rs.kotlinx.serialization.asMessageBodyWriter
import kotlinx.serialization.json.Json
import org.glassfish.jersey.jetty.JettyHttpContainerFactory
import org.glassfish.jersey.server.ResourceConfig
import javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE
import javax.ws.rs.core.UriBuilder

fun main() {
  val port = System.getenv("PORT")?.toIntOrNull() ?: 8084
  val uri = UriBuilder.fromUri("http://localhost").port(port).build()

  val resourceConfig = ResourceConfig().apply {
    register(Json.asMessageBodyWriter(APPLICATION_JSON_TYPE))
    register(ApiResource::class.java)
    register(PrintingEventListener)
  }

  JettyHttpContainerFactory.createServer(uri, resourceConfig).apply {
    stopAtShutdown = true
  }
}
