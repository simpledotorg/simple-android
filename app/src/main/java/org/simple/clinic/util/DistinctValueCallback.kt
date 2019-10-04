package org.simple.clinic.util

class DistinctValueCallback<T> {
  private var cachedValue: T? = null

  fun pass(newValue: T, callback: (T) -> Unit) {
    if (cachedValue != newValue) {
      callback(newValue)
      cachedValue = newValue
    }
  }
}
