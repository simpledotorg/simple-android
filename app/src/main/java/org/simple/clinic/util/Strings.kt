package org.simple.clinic.util

fun String?.nullIfBlank(): String? {
  return when {
    isNullOrBlank() -> null
    else -> this
  }
}

fun String?.valueOrEmpty(): String =
    this ?: ""
