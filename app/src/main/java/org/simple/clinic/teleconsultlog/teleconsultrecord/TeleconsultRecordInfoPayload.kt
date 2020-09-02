package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.medicalhistory.Answer
import java.util.UUID

@JsonClass(generateAdapter = true)
data class TeleconsultRecordInfoPayload(
    @Json(name = "recorded_at")
    val recordedAt: String,

    @Json(name = "teleconsultation_type")
    val teleconsultationType: TeleconsultationType,

    @Json(name = "patient_took_medicines")
    val patientTookMedicines: Answer,

    @Json(name = "patient_consented")
    val patientConsented: Answer,

    @Json(name = "medical_officier_number")
    val medicalOfficerNumber: String,

    @Json(name = "prescription_drugs")
    val prescriptionDrugs: List<UUID>
) {
}
