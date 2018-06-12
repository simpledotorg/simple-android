package org.resolvetosavelives.red.util

fun String.nullIfBlank(): String? {
  return when {
    isBlank() -> null
    else -> this
  }
}
