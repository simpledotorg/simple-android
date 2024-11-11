package org.simple.clinic

import org.simple.clinic.util.MinimumMemoryChecker

class FakeMinimumMemoryChecker : MinimumMemoryChecker {

  var hasMinMemory: Boolean = true

  override fun hasMinimumRequiredMemory(): Boolean {
    return hasMinMemory
  }
}
