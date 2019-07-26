package com.jakewharton.sdksearch

import dagger.android.DispatchingAndroidInjector

interface AppComponent {
  val androidInjector: DispatchingAndroidInjector<Any>
}
