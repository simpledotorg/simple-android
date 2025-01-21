package org.simple.clinic.util

class TestMinimumMemoryChecker : MinimumMemoryChecker {

  private var hasMinimumMemory: Boolean = false

  override fun hasMinimumRequiredMemory(): Boolean {
    return hasMinimumMemory
  }

  fun update(hasMinimumMemory: Boolean) {
    this.hasMinimumMemory = hasMinimumMemory
  }
}
