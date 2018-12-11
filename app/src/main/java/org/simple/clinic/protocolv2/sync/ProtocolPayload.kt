package org.simple.clinic.protocolv2.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.protocolv2.Protocol
import org.simple.clinic.protocolv2.ProtocolDrug
import org.threeten.bp.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class ProtocolPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "name")
    val name: String,

    @Json(name = "follow_up_days")
    val followUpDays: Int,

    @Json(name = "protocol_drugs")
    val protocolDrugs: List<ProtocolDrugPayload>
) {

  fun toDatabaseModel(newStatus: SyncStatus): Protocol {
    return Protocol(
        uuid = uuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        followUpDays = followUpDays,
        syncStatus = newStatus
    )
  }
}

@JsonClass(generateAdapter = true)
data class ProtocolDrugPayload(

    @Json(name = "uuid")
    val uuid: UUID,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "protocol_id")
    val protocolId: UUID,

    @Json(name = "rxnorm_code")
    val rxNormCode: String,

    @Json(name = "dosage")
    val dosage: String,

    @Json(name = "name")
    val name: String
) {

  fun toDatabaseModel(): ProtocolDrug {
    return ProtocolDrug(
        uuid = uuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        protocolUuid = protocolId,
        rxNormCode = rxNormCode,
        dosage = dosage,
        name = name
    )
  }
}
