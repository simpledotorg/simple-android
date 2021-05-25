package org.simple.clinic.util

import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.Optional as JOptional

/**
 * Modified to use [Just] instead of Just.
 *
 * Source: KOptional (https://github.com/gojuno/koptional).
 * Copyright 2017 Juno, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
sealed class Optional<T>(
    private val wrapped: JOptional<T>
) {
  fun get(): T = wrapped.get()

  fun filter(predicate: Predicate<T>): Optional<T> = fromJavaOptional(wrapped.filter(predicate))

  fun isPresent(): Boolean = wrapped.isPresent

  fun ifPresent(consumer: Consumer<T>) {
    wrapped.ifPresent(consumer)
  }

  fun <U> map(mapper: Function<T, U>): Optional<U> = fromJavaOptional(wrapped.map(mapper))

  fun <U> flatMap(mapper: Function<T, Optional<U>>): Optional<U> {
    return if (isPresent()) mapper.apply(get()) else None()
  }

  fun orElse(other: T): T = wrapped.orElse(other)

  fun orElseGet(other: Supplier<T>): T = wrapped.orElseGet(other)

  fun <X : Throwable> orElseThrow(exceptionSupplier: Supplier<X>): T = wrapped.orElseThrow(exceptionSupplier)

  override fun toString(): String {
    return wrapped.toString()
  }

  /**
   * Unwraps this optional into the value it holds or null if there is no value held.
   */
  @Deprecated(message = """
    Destructuring `Optional` is deprecated since we are moving to the Java Optional class (https://github.com/simpledotorg/simple-android/issues/1381).
    
    Do not use this anymore and use some of the alternate methods on the class.
  """)
  @Suppress("DeprecatedCallableAddReplaceWith")
  operator fun component1(): T? = if (isPresent()) get() else null

  companion object {

    private fun <T> fromJavaOptional(optional: JOptional<T>): Optional<T> {
      return if (optional.isPresent) Just(optional.get()) else None()
    }

    fun <T> empty(): Optional<T> = None()

    fun <T> of(value: T): Optional<T> = Just(value)

    fun <T> ofNullable(value: T?): Optional<T> = if (value != null) Just(value) else None()
  }
}

@Deprecated(
    message = """
    `Just` is deprecated since we are moving to the Java Optional class (https://github.com/simpledotorg/simple-android/issues/1381).
    
    Do not use this anymore and use the factory methods on `Optional` directly.
  """
)
class Just<T>
@Deprecated(
    message = "Use `Optional.of()` instead",
    replaceWith = ReplaceWith(
        expression = "Optional.of(value)",
        imports = ["org.simple.clinic.util.Optional"]
    )
)
constructor(
    val value: T
) : Optional<T>(JOptional.of(value)) {

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other == null || other !is Just<*> -> false
      else -> other.value == value
    }
  }

  override fun hashCode(): Int = value.hashCode()
}

@Deprecated(
    message = """
    `None` is deprecated since we are moving to the Java Optional class (https://github.com/simpledotorg/simple-android/issues/1381).
    
    Do not use this anymore and use the factory methods on `Optional` directly.
  """
)
class None<T>
@Deprecated(
    message = "Use `Optional.empty()` instead",
    replaceWith = ReplaceWith(
        expression = "Optional.empty()",
        imports = ["org.simple.clinic.util.Optional"]
    )
)
constructor() : Optional<T>(JOptional.empty()) {

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other == null || other !is None<*> -> false
      else -> true
    }
  }

  override fun hashCode(): Int {
    return Objects.hash(null)
  }
}

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
fun <T> T?.toOptional(): Optional<T> = Optional.ofNullable(this)

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
