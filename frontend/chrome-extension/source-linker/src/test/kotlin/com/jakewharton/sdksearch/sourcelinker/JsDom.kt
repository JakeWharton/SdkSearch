@file:JsModule("jsdom")
@file:JsNonModule

package com.jakewharton.sdksearch.sourcelinker

import org.w3c.dom.Window

@JsName("JSDOM")
external class JsDom(html: String) {
  val window: Window
}
