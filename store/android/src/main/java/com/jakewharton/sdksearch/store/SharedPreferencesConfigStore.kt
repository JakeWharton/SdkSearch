package com.jakewharton.sdksearch.store

import android.content.SharedPreferences
import androidx.core.content.edit

private const val KEY_GIT_WEB_URL = "git_web_url"
private const val KEY_DAC_URL = "dac_url"

class SharedPreferencesConfigStore(
  private val preferences: SharedPreferences,
  private val defaultGitWebUrl: String,
  private val defaultDacUrl: String
) : ConfigStore {
  override suspend fun load(): Config {
    val gitWebUrl = preferences.getString(KEY_GIT_WEB_URL, defaultGitWebUrl)
    val dacUrl = preferences.getString(KEY_DAC_URL, defaultDacUrl)
    return Config(gitWebUrl, dacUrl)
  }

  override suspend fun save(config: Config) {
    preferences.edit {
      putString(KEY_GIT_WEB_URL, config.gitWebUrl)
      putString(KEY_DAC_URL, config.dacUrl)
    }
  }
}
