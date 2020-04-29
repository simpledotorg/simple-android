package org.simple.clinic.util

class ValueChangedCallback<T> {
  private var cachedValue: T? = null

  fun pass(newValue: T, callback: (T) -> Unit) {
    if (cachedValue != newValue) {
      callback(newValue)
      cachedValue = newValue
    }
  }
}
