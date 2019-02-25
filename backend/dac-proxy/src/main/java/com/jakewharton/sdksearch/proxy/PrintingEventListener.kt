package com.jakewharton.sdksearch.proxy

import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEvent.Type.FINISHED
import org.glassfish.jersey.server.monitoring.RequestEventListener

internal object PrintingEventListener : ApplicationEventListener, RequestEventListener {
  override fun onRequest(requestEvent: RequestEvent): RequestEventListener {
    println(buildString {
      append(">>> ")

      val request = requestEvent.containerRequest
      append(request.method)
      append(' ')
      append(request.requestUri)

      val mediaType = request.mediaType
      if (mediaType != null) {
        append(" (")
        append(mediaType)
        append(')')
      }
    })
    return this
  }

  override fun onEvent(event: RequestEvent) {
    if (event.type == FINISHED) {
      event.exception?.printStackTrace()

      val request = event.containerRequest
      val response = event.containerResponse
      println("<<< ${response.status} ${request.requestUri} (${response.mediaType})")
    }
  }

  override fun onEvent(event: ApplicationEvent) {}
}
