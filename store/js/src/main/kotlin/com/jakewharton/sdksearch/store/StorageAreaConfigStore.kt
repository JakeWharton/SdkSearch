package com.jakewharton.sdksearch.store

import com.chrome.platform.storage.StorageArea
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.js.json

private const val KEY_GIT_WEB_URL = "git_web_url"
private const val KEY_DAC_URL = "dac_url"

class StorageAreaConfigStore(private val storageArea: StorageArea) : ConfigStore {
  override suspend fun load() = suspendCoroutine<Config> { continuation ->
    storageArea.get(json(
        KEY_GIT_WEB_URL to PRODUCTION_GIT_WEB,
        KEY_DAC_URL to PRODUCTION_DAC
    )) {
      val gitWebUrl = it[KEY_GIT_WEB_URL] as String
      val dacUrl = it[KEY_DAC_URL] as String
      val config = Config(gitWebUrl, dacUrl)
      continuation.resume(config)
    }
  }

  override suspend fun save(config: Config) = suspendCoroutine<Unit> { continuation ->
    storageArea.set(json(
        KEY_GIT_WEB_URL to config.gitWebUrl,
        KEY_DAC_URL to config.dacUrl
    )) {
      continuation.resume(Unit)
    }
  }
}
