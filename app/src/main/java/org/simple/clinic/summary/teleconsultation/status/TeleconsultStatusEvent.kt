package org.simple.clinic.summary.teleconsultation.status

import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultStatusEvent : UiEvent

data class TeleconsultStatusChanged(val teleconsultStatus: TeleconsultStatus) : TeleconsultStatusEvent() {
  override val analyticsName: String = "Teleconsult Status Sheet:Status changed to $teleconsultStatus"
}

object TeleconsultStatusUpdated : TeleconsultStatusEvent()

object DoneClicked : TeleconsultStatusEvent() {
  override val analyticsName: String = "Teleconsult Status Sheet:Done Clicked"
}
