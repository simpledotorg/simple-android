package org.resolvetosavelives.red.util

/**
 * Modified to use [Just] instead of Some.
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

  fun toNullable(): T? = when (this) {
    is Just -> value
    is None -> null
  }
}

data class Just<out T : Any>(val value: T) : Optional<T>()

object None : Optional<Nothing>()

@Suppress("unused")
fun <T : Any> T?.toOptional(): Optional<T> = if (this == null) None else Just(this)
