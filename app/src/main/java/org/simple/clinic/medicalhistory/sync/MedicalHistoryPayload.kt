package org.simple.clinic.medicalhistory.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.medicalhistory.Answer
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class MedicalHistoryPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Deprecated(
        message = "This property is no longer in use and has been left here for API compatibility purposes. Scheduled to be removed in api v4."
    )
    @Json(name = "diagnosed_with_hypertension")
    val diagnosedWithHypertension: Answer,

    @Json(name = "receiving_treatment_for_hypertension")
    val isOnTreatmentForHypertension: Answer,

    @Json(name = "prior_heart_attack")
    val hasHadHeartAttack: Answer,

    @Json(name = "prior_stroke")
    val hasHadStroke: Answer,

    @Json(name = "chronic_kidney_disease")
    val hasHadKidneyDisease: Answer,

    @Json(name = "diabetes")
    val hasDiabetes: Answer,

    // TODO(vs): 2020-01-30 Make this non-nullable once the server changes are in production
    @Json(name = "hypertension")
    val hasHypertension: Answer?,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,
    @Json(name = "deleted_at")
    val deletedAt: Instant?
)
