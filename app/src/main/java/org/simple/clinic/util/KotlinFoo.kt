package org.simple.clinic.util

// Forces when blocks to be exhaustive.
fun Unit.exhaustive() {}

fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)
