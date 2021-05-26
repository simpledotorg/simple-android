package org.simple.clinic.summary.teleconsultation.status

import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import java.util.UUID

sealed class TeleconsultStatusEffect

data class UpdateTeleconsultStatus(
    val teleconsultRecordId: UUID,
    val teleconsultStatus: TeleconsultStatus
) : TeleconsultStatusEffect()

object CloseSheet : TeleconsultStatusEffect()
