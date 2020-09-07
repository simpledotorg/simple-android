package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import java.util.UUID

sealed class TeleconsultRecordEffect

object GoBack : TeleconsultRecordEffect()

data class NavigateToTeleconsultSuccess(val teleconsultRecordId: UUID) : TeleconsultRecordEffect()

data class LoadTeleconsultRecordWithPrescribedDrugs(val teleconsultRecordId: UUID) : TeleconsultRecordEffect()

data class CreateTeleconsultRecord(
    val teleconsultRecordId: UUID,
    val patientUuid: UUID,
    val teleconsultationType: TeleconsultationType,
    val patientTookMedicine: Answer,
    val patientConsented: Answer
) : TeleconsultRecordEffect()
