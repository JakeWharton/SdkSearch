@file:Suppress("NOTHING_TO_INLINE")

package com.jakewharton.sdksearch.util

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException

inline fun <I> Observable<I>.crashingSubscribe(noinline onNext: (I) -> Unit): Disposable {
  return subscribe(onNext, { throw OnErrorNotImplementedException(it) })
}

inline fun Disposable.addTo(disposables: CompositeDisposable) = disposables.add(this)

inline fun <reified T> Observable<*>.ofType(): Observable<T> = ofType(T::class.java)
