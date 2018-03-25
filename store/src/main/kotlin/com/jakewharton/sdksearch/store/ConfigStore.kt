package com.jakewharton.sdksearch.store

interface ConfigStore {
  suspend fun load(): Config
  suspend fun save(config: Config)
}
