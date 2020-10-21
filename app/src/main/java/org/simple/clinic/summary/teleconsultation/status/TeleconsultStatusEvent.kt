package org.simple.clinic.summary.teleconsultation.status

import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus

sealed class TeleconsultStatusEvent

data class TeleconsultStatusChanged(val teleconsultStatus: TeleconsultStatus) : TeleconsultStatusEvent()

object TeleconsultStatusUpdated : TeleconsultStatusEvent()
