package org.resolvetosavelives.red.util

sealed class Optional<out T : Any> {

  fun toNullable(): T? = when (this) {
    is Just -> value
    is None -> null
  }
}

data class Just<out T : Any>(val value: T) : Optional<T>()

object None : Optional<Nothing>()

@Suppress("unused")
fun <T : Any> T?.toOptional(): Optional<T> = if (this == null) None else Just(this)
