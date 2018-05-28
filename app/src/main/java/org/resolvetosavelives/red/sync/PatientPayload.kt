package org.resolvetosavelives.red.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.newentry.search.PatientStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

@JsonClass(generateAdapter = true)
data class PatientPayload(

    @Json(name = "id")
    val uuid: String,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "gender")
    val gender: Gender,

    @Json(name = "date_of_birth")
    val dateOfBirth: LocalDate?,

    @Json(name = "age")
    val ageWhenCreated: Int?,

    @Json(name = "status")
    val status: PatientStatus,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "address")
    val address: PatientAddressPayload
)

@JsonClass(generateAdapter = true)
data class PatientAddressPayload(
    @Json(name = "id")
    val uuid: String,

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
)
