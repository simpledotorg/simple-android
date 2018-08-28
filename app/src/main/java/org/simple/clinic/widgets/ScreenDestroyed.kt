package org.simple.clinic.widgets

data class ScreenDestroyed(val name: String = "") : UiEvent {

  override val analyticsName = if (name.isNotBlank()) "Screen Destroyed:$name" else ""
}
