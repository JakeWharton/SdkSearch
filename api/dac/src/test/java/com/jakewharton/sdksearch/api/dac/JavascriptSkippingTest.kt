package com.jakewharton.sdksearch.api.dac

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class JavascriptSkippingTest {
  @get:Rule val server = MockWebServer()

  private lateinit var service: DocumentationService

  @Before fun before() {
    service = DacComponent.builder()
        .baseUrl(BaseUrl(server.url("/").toString()))
        .build()
        .documentationService()
  }

  @Test fun skipsJavascript() = runBlocking<Unit> {
    server.enqueue(MockResponse().setBody("""
      |var DATA = [
      |  { id:1, label:"android.Manifest", link:"reference/android/Manifest.html", type:"class", deprecated:"false" }
      |];
      |
      """.trimMargin()))

    val items = service.list("whatever").await()
    assertThat(items).containsExactly(
        Item(1, "android.Manifest", "reference/android/Manifest.html", "class", deprecated = false))
  }
}
