package org.simple.clinic.setup.runcheck

sealed class AllowedToRun

data object Allowed : AllowedToRun()

data class Disallowed(val reason: Reason) : AllowedToRun() {

  enum class Reason {
    Rooted
  }
}
