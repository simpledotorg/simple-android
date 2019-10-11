package org.simple.clinic.encounter.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
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

    val observations: List<EncounterObservations>
)

@JsonClass(generateAdapter = true)
data class EncounterObservations(

    @Json(name = "blood_pressures")
    val bloodPressureMeasurements: List<BloodPressureMeasurementPayload>
)
