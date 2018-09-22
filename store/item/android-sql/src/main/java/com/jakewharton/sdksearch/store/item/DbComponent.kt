package com.jakewharton.sdksearch.store.item

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@Component(modules = [
  DbModule::class
])
interface DbComponent {
  fun itemStore(): ItemStore

  @Component.Builder
  interface Builder {
    @BindsInstance fun context(context: Context): Builder
    @BindsInstance fun coroutineContext(coroutineContext: CoroutineContext): Builder
    @BindsInstance fun filename(filename: String?): Builder
    fun build(): DbComponent
  }

  companion object {
    fun builder(): Builder = DaggerDbComponent.builder()
  }
}
