package org.simple.clinic.analytics

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

  fun reportUserInteraction(name: String) {
    reporters.forEach { it.safeReport("Error reporting interaction!") { createEvent("UserInteraction", mapOf("name" to name)) } }
  }

  fun reportScreenChange(outgoingScreen: String, incomingScreen: String) {
    reporters.forEach { it.safeReport { createEvent("ScreenChange", mapOf("outgoing" to outgoingScreen, "incoming" to incomingScreen)) } }
  }
}
