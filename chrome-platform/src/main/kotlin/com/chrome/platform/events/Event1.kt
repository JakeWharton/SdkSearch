package com.chrome.platform.events

/** https://developer.chrome.com/extensions/events#type-Event */
@JsName("Event")
open external class Event1<out T> : Event0 {
  /** https://developer.chrome.com/extensions/events#method-Event-addListener */
  fun addListener(callback: (T) -> Unit)

  /** https://developer.chrome.com/extensions/events#method-Event-removeListener */
  fun removeListener(callback: (T) -> Unit)

  /** https://developer.chrome.com/extensions/events#method-Event-hasListener */
  fun hasListener(callback: (T) -> Unit)
}
