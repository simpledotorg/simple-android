package org.simple.clinic.patient.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.nameToSearchableForm
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
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

    @Json(name = "deleted_at")
    val deletedAt: Instant?,

    @Json(name = "address")
    val address: PatientAddressPayload,

    @Json(name = "phone_numbers")
    val phoneNumbers: List<PatientPhoneNumberPayload>?,

    @Json(name = "business_identifiers")
    val businessIds: List<BusinessIdPayload>
) {

  fun toDatabaseModel(newStatus: SyncStatus): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = address.uuid,
        fullName = fullName,
        searchableName = nameToSearchableForm(fullName),
        gender = gender,
        dateOfBirth = dateOfBirth,
        age = age?.let {
          Age(age, ageUpdatedAt!!, computedDateOfBirth(age, ageUpdatedAt))
        },
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        // recordedAt should be changed here when Patient payload is updated
        recordedAt = createdAt,
        syncStatus = newStatus)
  }

  private fun computedDateOfBirth(years: Int, updatedAt: Instant): LocalDate {
    val updatedAtLocalDateTime = LocalDateTime.ofInstant(updatedAt, ZoneOffset.UTC)
    val updatedAtLocalDate = LocalDate.of(updatedAtLocalDateTime.year, updatedAtLocalDateTime.month, updatedAtLocalDateTime.dayOfMonth)

    return updatedAtLocalDate.minusYears(years.toLong())
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
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?
) {

  fun toDatabaseModel(): PatientAddress {
    return PatientAddress(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        district = district,
        state = state,
        country = country,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,

        // recordedAt should be changed here when Patient payload is updated
        recordedAt = createdAt)
  }
}

@JsonClass(generateAdapter = true)
data class PatientPhoneNumberPayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "number")
    val number: String,

    @Json(name = "phone_type")
    val type: PatientPhoneNumberType,

    @Json(name = "active")
    val active: Boolean,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?
) {

  fun toDatabaseModel(uuidOfPatient: UUID): PatientPhoneNumber {
    return PatientPhoneNumber(
        uuid = uuid,
        patientUuid = uuidOfPatient,
        number = number,
        phoneType = type,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,

        // recordedAt should be changed here when Patient payload is updated
        recordedAt = createdAt
    )
  }
}

@JsonClass(generateAdapter = true)
data class BusinessIdPayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "identifier")
    val identifier: String,

    @Json(name = "identifier_type")
    val identifierType: Identifier.IdentifierType,

    @Json(name = "metadata_version")
    val metaDataVersion: BusinessId.MetaDataVersion,

    @Json(name = "metadata")
    val metaData: String,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?
) {

  fun toDatabaseModel(patientUuid: UUID): BusinessId {
    return BusinessId(
        uuid = uuid,
        patientUuid = patientUuid,
        identifier = Identifier(value = identifier, type = identifierType),
        metaDataVersion = metaDataVersion,
        metaData = metaData,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,

        // recordedAt should be changed here when Patient payload is updated
        recordedAt = createdAt
    )
  }
}
