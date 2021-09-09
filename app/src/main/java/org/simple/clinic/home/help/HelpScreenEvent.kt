package org.simple.clinic.home.help

import org.simple.clinic.help.HelpPullResult
import org.simple.clinic.widgets.UiEvent
import java.util.Optional

sealed class HelpScreenEvent : UiEvent

object HelpScreenTryAgainClicked : HelpScreenEvent() {
  override val analyticsName: String = "Help Screen:Try Again Clicked"
}

data class HelpContentLoaded(val helpContent: Optional<String>) : HelpScreenEvent()

data class HelpSyncPullResult(val result: HelpPullResult) : HelpScreenEvent()
