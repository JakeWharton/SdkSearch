package com.jakewharton.sdksearch.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class JavascriptSkippingTest {
  @get:Rule val server = MockWebServer()

  private lateinit var service: DocumentationService

  @Before fun before() {
    service = ApiComponent.builder()
        .baseUrl(server.url("/"))
        .build()
        .documentationService()
  }

  @Test fun skipsJavascript() {
    server.enqueue(MockResponse().setBody("""
      |var DATA = [
      |  { id:1, label:"android.Manifest", link:"reference/android/Manifest.html", type:"class", deprecated:"false" }
      |];
      |
      """.trimMargin()))

    service.list("whatever")
        .test()
        .await()
        .assertValue(listOf(ApiItem(1, "android.Manifest", "reference/android/Manifest.html", "class")))
        .assertComplete()
  }
}
