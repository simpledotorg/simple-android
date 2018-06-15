package org.simple.clinic.util

fun String.nullIfBlank(): String? {
  return when {
    isBlank() -> null
    else -> this
  }
}
