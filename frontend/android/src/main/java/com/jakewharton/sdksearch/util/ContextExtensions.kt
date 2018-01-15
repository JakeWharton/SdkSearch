package com.jakewharton.sdksearch.util

import android.content.Context

inline fun <reified T : Any> Context.systemService(): T = getSystemService(T::class.java)
