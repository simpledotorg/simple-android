package org.simple.clinic.home.help

import org.simple.clinic.widgets.UiEvent

sealed class HelpScreenEvent : UiEvent

object HelpScreenTryAgainClicked: HelpScreenEvent() {
  override val analyticsName: String = "Help Screen:Try Again Clicked"
}
