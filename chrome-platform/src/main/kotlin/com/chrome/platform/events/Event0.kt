package com.chrome.platform.events

/** https://developer.chrome.com/extensions/events#type-Event */
@JsName("Event")
open external class Event0 : Event {
  /** https://developer.chrome.com/extensions/events#method-Event-addListener */
  fun addListener(callback: () -> Unit)

  /** https://developer.chrome.com/extensions/events#method-Event-removeListener */
  fun removeListener(callback: () -> Unit)

  /** https://developer.chrome.com/extensions/events#method-Event-hasListener */
  fun hasListener(callback: () -> Unit)
}
