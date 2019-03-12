package org.simple.clinic.router.screen

import androidx.annotation.VisibleForTesting

class ScreenResults {

  private val results = mutableMapOf<String, Any?>()

  fun put(key: String, value: Any?) {
    results[key] = value
  }

  fun consume(key: String): Any? {
    val storedResult = results[key]

    results.remove(key)

    return storedResult
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun keys(): Set<String> {
    return results.keys
  }
}
