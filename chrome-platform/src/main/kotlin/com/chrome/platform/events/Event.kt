@file:JsQualifier("chrome.events")

package com.chrome.platform.events

/** https://developer.chrome.com/extensions/events#type-Event */
sealed external class Event<in T : Any> {
  /** https://developer.chrome.com/extensions/events#method-Event-hasListeners */
  fun hasListeners(): Boolean

  /** https://developer.chrome.com/extensions/events#method-Event-addListener */
  fun addListener(callback: T)

  /** https://developer.chrome.com/extensions/events#method-Event-removeListener */
  fun removeListener(callback: T)

  /** https://developer.chrome.com/extensions/events#method-Event-hasListener */
  fun hasListener(callback: T)
}
