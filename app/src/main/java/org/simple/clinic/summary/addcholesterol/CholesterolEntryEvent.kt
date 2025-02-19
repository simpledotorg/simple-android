package org.simple.clinic.summary.addcholesterol

import org.simple.clinic.widgets.UiEvent

sealed interface CholesterolEntryEvent : UiEvent

data class CholesterolChanged(val cholesterolValue: Float) : CholesterolEntryEvent

data object SaveClicked : CholesterolEntryEvent {
  override val analyticsName: String = "Cholesterol Entry:Save Clicked"
}

data object CholesterolSaved : CholesterolEntryEvent

data object KeyboardClosed : CholesterolEntryEvent
