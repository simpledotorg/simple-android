package org.simple.clinic.medicalhistory.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class MedicalHistoryPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Json(name = "diagnosed_with_hypertension")
    val diagnosedWithHypertension: Boolean?,

    @Json(name = "receiving_treatment_for_hypertension")
    val isOnTreatmentForHypertension: Boolean?,

    @Json(name = "prior_heart_attack")
    val hasHadHeartAttack: Boolean?,

    @Json(name = "prior_stroke")
    val hasHadStroke: Boolean?,

    @Json(name = "chronic_kidney_disease")
    val hasHadKidneyDisease: Boolean?,

    @Json(name = "diabetes")
    val hasDiabetes: Boolean?,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
)
