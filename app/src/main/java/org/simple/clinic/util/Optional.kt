package org.simple.clinic.util

import java.util.Optional

/**
 * Wraps an instance of T (or null) into an [Optional]:
 *
 * ```kotlin
 * val a: String? = "str"
 * val b: String? = null
 *
 * val optionalA = a.toOptional() // Just("str")
 * val optionalB = b.toOptional() // None
 * ```
 *
 * This is the preferred method of obtaining an instance of [Optional] in Kotlin. In Java, prefer
 * using the static [Optional.ofNullable] method.
 */
fun <T> T?.toOptional(): Optional<T> = Optional.ofNullable(this) as Optional<T>

/**
 * Converts [Optional] to either its non-null value if it's non-empty or `null` if it's empty.
 */
fun <T> Optional<T>.toNullable(): T? = if (isPresent()) get() else null

@Deprecated(
    message = "Use .isPresent() instead.",
    replaceWith = ReplaceWith("!isPresent()")
)
fun Optional<*>.isEmpty(): Boolean = !isPresent()

@Deprecated(
    message = "Use .isPresent() instead.",
    replaceWith = ReplaceWith("isPresent()")
)
fun Optional<*>.isNotEmpty(): Boolean = isPresent()
