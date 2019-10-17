package org.simple.clinic.encounter.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.encounter.Encounter
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@JsonClass(generateAdapter = true)
data class EncounterPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Json(name = "encountered_on")
    val encounteredOn: LocalDate,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,

    @Json(name = "observations")
    val observations: EncounterObservationsPayload
) {

  fun toDatabaseModel(syncStatus: SyncStatus) =
      Encounter(
          uuid = uuid,
          patientUuid = patientUuid,
          encounteredOn = encounteredOn,
          syncStatus = syncStatus,
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      )
}

@JsonClass(generateAdapter = true)
data class EncounterObservationsPayload(

    @Json(name = "blood_pressures")
    val bloodPressureMeasurements: List<BloodPressureMeasurementPayload>
)
