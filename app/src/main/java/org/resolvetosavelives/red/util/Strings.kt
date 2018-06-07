package org.resolvetosavelives.red.util

public fun String.nullIfBlank(): String? {
  return when {
    isBlank() -> null
    else -> this
  }
}
