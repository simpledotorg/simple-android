package org.simple.clinic.analytics

import java.util.UUID

object Analytics {

  private var reporters: List<Reporter> = emptyList()

  fun addReporter(vararg reportersToAdd: Reporter) {
    reporters += reportersToAdd
  }

  fun clearReporters() {
    reporters = emptyList()
  }

  fun removeReporter(reporter: Reporter) {
    reporters -= reporter
  }

  fun setUserId(uuid: UUID) {
    reporters.forEach { it.safely("Error setting user id") { setUserIdentity(uuid.toString()) } }
  }

  fun reportUserInteraction(name: String) {
    reporters.forEach { it.safely("Error reporting interaction!") { createEvent("UserInteraction", mapOf("name" to name)) } }
  }

  fun reportScreenChange(outgoingScreen: String, incomingScreen: String) {
    reporters.forEach { it.safely("Error reporting screen change!") { createEvent("ScreenChange", mapOf("outgoing" to outgoingScreen, "incoming" to incomingScreen)) } }
  }

  fun reportInputValidationError(error: String) {
    reporters.forEach { it.safely("Error reporting input validation error") { createEvent("InputValidationError", mapOf("name" to error)) } }
  }
}
