package org.simple.clinic.util

import io.reactivex.Maybe
import io.reactivex.Single

// Forces when blocks to be exhaustive.
fun Unit.exhaustive() {}

inline fun <reified U> Single<*>.ofType(): Maybe<U> =
    filter { it is U }.cast(U::class.java)
