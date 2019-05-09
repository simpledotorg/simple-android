package org.simple.clinic.medicalhistory.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.medicalhistory.MedicalHistory
import org.threeten.bp.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class MedicalHistoryPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Json(name = "diagnosed_with_hypertension")
    val diagnosedWithHypertension: MedicalHistory.Answer,

    @Json(name = "receiving_treatment_for_hypertension")
    val isOnTreatmentForHypertension: MedicalHistory.Answer,

    @Json(name = "prior_heart_attack")
    val hasHadHeartAttack: MedicalHistory.Answer,

    @Json(name = "prior_stroke")
    val hasHadStroke: MedicalHistory.Answer,

    @Json(name = "chronic_kidney_disease")
    val hasHadKidneyDisease: MedicalHistory.Answer,

    @Json(name = "diabetes")
    val hasDiabetes: MedicalHistory.Answer,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,

    @Json(name = "recorded_at")
    val recordedAt: Instant?
)
