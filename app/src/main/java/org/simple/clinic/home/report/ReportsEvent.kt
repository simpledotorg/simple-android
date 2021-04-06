package org.simple.clinic.home.report

import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

sealed class ReportsEvent : UiEvent

data class ReportsLoaded(val reportsContent: Optional<String>) : ReportsEvent()

object WebBackClicked : ReportsEvent() {

  override val analyticsName = "Reports Screen:Back clicked"
}
