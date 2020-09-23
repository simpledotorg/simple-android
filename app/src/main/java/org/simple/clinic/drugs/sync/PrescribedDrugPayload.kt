package org.simple.clinic.drugs.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class PrescribedDrugPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "name")
    val name: String,

    @Json(name = "dosage")
    val dosage: String?,

    @Json(name = "rxnorm_code")
    val rxNormCode: String?,

    @Json(name = "is_deleted")
    val isDeleted: Boolean,

    @Json(name = "is_protocol_drug")
    val isProtocolDrug: Boolean,

    @Json(name = "patient_id")
    val patientId: UUID,

    @Json(name = "facility_id")
    val facilityId: UUID,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,

    @Json(name = "frequency")
    val frequency: MedicineFrequency?,

    @Json(name = "duration_in_days")
    val durationInDays: Int?,

    @Json(name = "teleconsultation_id")
    val teleconsultationId: UUID?
) {

  fun toDatabaseModel(syncStatus: SyncStatus): PrescribedDrug {
    return PrescribedDrug(
        uuid = uuid,
        name = name,
        dosage = dosage,
        rxNormCode = rxNormCode,
        isDeleted = isDeleted,
        isProtocolDrug = isProtocolDrug,
        patientUuid = patientId,
        facilityUuid = facilityId,
        syncStatus = syncStatus,
        timestamps = Timestamps(createdAt, updatedAt, deletedAt),
        frequency = frequency,
        durationInDays = durationInDays,
        teleconsultationId = teleconsultationId
    )
  }
}
