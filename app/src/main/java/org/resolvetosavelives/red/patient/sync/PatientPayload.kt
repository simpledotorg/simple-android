package org.resolvetosavelives.red.patient.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.resolvetosavelives.red.patient.Age
import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.patient.Patient
import org.resolvetosavelives.red.patient.PatientAddress
import org.resolvetosavelives.red.patient.PatientStatus
import org.resolvetosavelives.red.patient.SyncStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@JsonClass(generateAdapter = true)
data class PatientPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "gender")
    val gender: Gender,

    @Json(name = "date_of_birth")
    val dateOfBirth: LocalDate?,

    @Json(name = "age")
    val age: Int?,

    @Json(name = "age_updated_at")
    val ageUpdatedAt: Instant?,

    @Json(name = "status")
    val status: PatientStatus,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "address")
    val address: PatientAddressPayload
) {

  fun toDatabaseModel(updatedStatus: SyncStatus): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = address.uuid,
        fullName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        age = age?.let {
          Age(value = age, updatedAt = ageUpdatedAt!!)
        },
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = updatedStatus)
  }
}

@JsonClass(generateAdapter = true)
data class PatientAddressPayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "village_or_colony")
    val colonyOrVillage: String?,

    @Json(name = "district")
    val district: String,

    @Json(name = "state")
    val state: String,

    @Json(name = "country")
    val country: String? = "India",

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
) {

  fun toDatabaseModel(): PatientAddress {
    return PatientAddress(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        district = district,
        state = state,
        country = country,
        createdAt = createdAt,
        updatedAt = updatedAt)
  }
}
