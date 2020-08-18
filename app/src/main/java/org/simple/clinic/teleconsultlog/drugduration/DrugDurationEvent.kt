package org.simple.clinic.teleconsultlog.drugduration

import org.simple.clinic.widgets.UiEvent

sealed class DrugDurationEvent : UiEvent

data class DurationChanged(val duration: String) : DrugDurationEvent() {
  override val analyticsName: String = "Drug Duration:Duration Changed"
}

object DrugDurationSaveClicked : DrugDurationEvent() {
  override val analyticsName: String = "Drug Duration Sheet:Save Clicked"
}
