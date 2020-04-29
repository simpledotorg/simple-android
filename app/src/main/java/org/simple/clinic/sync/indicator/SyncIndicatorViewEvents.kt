package org.simple.clinic.sync.indicator

import org.simple.clinic.widgets.UiEvent

object SyncIndicatorViewCreated : UiEvent

object SyncIndicatorViewClicked : UiEvent {
  override val analyticsName = "Sync Indicator: View Clicked"
}
