package org.simple.clinic.util

import java.util.UUID

fun String?.nullIfBlank(): String? {
  return when {
    isNullOrBlank() -> null
    else -> this
  }
}

fun String?.valueOrEmpty(): String =
    this ?: ""

fun String?.asUuid(): UUID? = try {
  UUID.fromString(this)
} catch (e: IllegalArgumentException) {
  null
}
