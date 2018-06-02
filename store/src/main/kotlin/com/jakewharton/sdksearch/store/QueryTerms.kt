package com.jakewharton.sdksearch.store

fun String.asTerms(): List<String> {
  var splitNext = false
  return this.fold(emptyList()) { segments, character ->
    // Fts cant handle these. Encode them as their unicode
    when (character) {
      '%' -> return@fold segments + "U0025"
      '_' -> return@fold segments + "U005F"
      '\\' -> return@fold segments + "U005C"
    }

    if (character !in 'A'..'Z' && character !in 'a'..'z') {
      splitNext = true
      return@fold segments
    }


    val lastSegment = segments.lastOrNull() ?: return@fold listOf(character.toString())
    if (character.toLowerCase() == character && !splitNext) {
      return@fold segments.dropLast(1) + (lastSegment + character)
    }
    splitNext = false
    return@fold segments + character.toString()
  }
}
