package org.simple.sharedTestCode.util

import org.simple.clinic.util.MinimumMemoryChecker

class TestMinimumMemoryChecker : MinimumMemoryChecker {

  private var hasMinimumMemory: Boolean = false

  override fun hasMinimumRequiredMemory(): Boolean {
    return hasMinimumMemory
  }

  fun update(hasMinimumMemory: Boolean) {
    this.hasMinimumMemory = hasMinimumMemory
  }
}
