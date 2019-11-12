package org.simple.clinic.util

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
sealed class Optional<out T : Any> {

  /**
   * Converts [Optional] to either its non-null value if it's [Just] or `null` if it's [None].
   */
  abstract fun toNullable(): T?

  /**
   * Unwraps this optional into the value it holds or null if there is no value held.
   */
  abstract operator fun component1(): T?

  fun isNotEmpty(): Boolean {
    return this is Just
  }

  fun isEmpty(): Boolean {
    return this is None
  }

  companion object {

    /**
     * Wraps an instance of T (or null) into an [Optional]:
     *
     * ```java
     * String a = "str";
     * String b = null;
     *
     * Optional<String> optionalA = Optional.toOptional(a); // Just("str")
     * Optional<String> optionalB = Optional.toOptional(b); // None
     * ```
     *
     * This is the preferred method of obtaining an instance of [Optional] in Java. In Kotlin,
     * prefer using the [toOptional][com.gojuno.koptional.toOptional] extension function.
     */
    @JvmStatic
    fun <T : Any> toOptional(value: T?): Optional<T> = if (value == null) None else Just(value)
  }
}

data class Just<out T : Any>(val value: T) : Optional<T>() {
  override fun toString() = "Just($value)"
  override fun toNullable(): T = value
}

object None : Optional<Nothing>() {
  override fun toString() = "None"

  override fun component1(): Nothing? = null

  override fun toNullable(): Nothing? = null
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
 * using the static [Optional.toOptional] method.
 */
fun <T : Any> T?.toOptional(): Optional<T> = if (this == null) None else Just(this)
