package org.simple.clinic.summary.teleconsultation.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class TeleconsultationFacilityInfoPayload(

    @Json(name = "id")
    val id: UUID,

    @Json(name = "facility_id")
    val facilityId: UUID,

    @Json(name = "medical_officers")
    val medicalOfficers: List<MedicalOfficerPayload>,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?
) {

  fun toTeleconsultInfoWithMedicalOfficersDatabaseModel(): TeleconsultationFacilityWithMedicalOfficers {
    return TeleconsultationFacilityWithMedicalOfficers(
        teleconsultationFacilityInfo = TeleconsultationFacilityInfo(
            teleconsultationFacilityId = id,
            facilityId = facilityId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            syncStatus = SyncStatus.DONE
        ),
        medicalOfficers = medicalOfficers.map {
          MedicalOfficer(
              medicalOfficerId = it.id,
              fullName = it.fullName,
              phoneNumber = it.phoneNumber
          )
        }
    )
  }
}
