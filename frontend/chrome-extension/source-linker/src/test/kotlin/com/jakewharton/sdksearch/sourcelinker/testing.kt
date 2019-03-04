package com.jakewharton.sdksearch.sourcelinker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

fun testBody(body: suspend CoroutineScope.() -> Unit) = GlobalScope.promise { body() }
