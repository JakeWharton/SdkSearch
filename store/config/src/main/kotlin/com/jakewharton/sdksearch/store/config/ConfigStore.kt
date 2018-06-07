package com.jakewharton.sdksearch.store.config

interface ConfigStore {
  suspend fun load(): Config
  suspend fun save(config: Config)
}
