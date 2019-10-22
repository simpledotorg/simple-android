package org.simple.clinic.util

import android.content.Context

// Forces when blocks to be exhaustive.
fun Unit.exhaustive() {}

fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

@Suppress("UNCHECKED_CAST")
fun <T> Context.service(name: String) = getSystemService(name) as T
