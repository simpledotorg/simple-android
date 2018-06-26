package org.simple.clinic.facility

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class FacilityPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "name")
    val name: String,

    @Json(name = "facility_type")
    val facilityType: String?,

    @Json(name = "street_address")
    val streetAddress: String?,

    @Json(name = "village_or_colony")
    val villageOrColony: String?,

    @Json(name = "district")
    val district: String,

    @Json(name = "state")
    val state: String,

    @Json(name = "country")
    val country: String = "India",

    @Json(name = "pin")
    val pinCode: String?,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
) {

  fun toDatabaseModel(syncStatus: SyncStatus): Facility {
    return Facility(
        uuid = uuid,
        name = name,
        facilityType = facilityType,
        streetAddress = streetAddress,
        villageOrColony = villageOrColony,
        district = district,
        state = state,
        country = country,
        pinCode = pinCode,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus)
  }
}
