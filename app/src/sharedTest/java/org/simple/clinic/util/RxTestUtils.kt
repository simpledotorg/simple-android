package org.simple.clinic.util

import io.reactivex.observers.TestObserver

fun <T> TestObserver<T>.assertLatestValue(value: T) {
  @Suppress("UnstableApiUsage")
  assertValueAt(valueCount() - 1, value)
}
