@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.sdksearch.util

import io.reactivex.Observable

inline fun <reified T> Observable<*>.ofType(): Observable<T> = ofType(T::class.java)
