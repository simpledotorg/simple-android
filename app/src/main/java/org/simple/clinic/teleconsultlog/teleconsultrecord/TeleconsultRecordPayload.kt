package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class TeleconsultRecordPayload(

    @Json(name = "id")
    val id: UUID,

    @Json(name = "patient_id")
    val patientId: UUID,

    @Json(name = "medical_officer_id")
    val medicalOfficerId: UUID,

    @Json(name = "request")
    val teleconsultRequestInfo: TeleconsultRequestInfoPayload?,

    @Json(name = "record")
    val teleconsultRecordInfo: TeleconsultRecordInfoPayload?,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant
)
