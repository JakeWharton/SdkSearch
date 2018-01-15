package com.jakewharton.sdksearch.util

import android.net.Uri

fun String.toUri(): Uri = Uri.parse(this)
