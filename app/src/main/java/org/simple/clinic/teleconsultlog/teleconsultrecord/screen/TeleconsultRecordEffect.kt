package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import java.util.UUID

sealed class TeleconsultRecordEffect

object GoBack : TeleconsultRecordEffect()

data class NavigateToTeleconsultSuccess(val teleconsultRecordId: UUID) : TeleconsultRecordEffect()
