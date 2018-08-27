package org.simple.clinic.widgets

data class ScreenCreated(val name: String = "") : UiEvent {

  override val analyticsName = if (name.isNotBlank()) "Screen Created:$name" else ""
}
