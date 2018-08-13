package org.simple.clinic

import io.bloco.faker.Faker
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

// TODO: Rename to DataFaker.
@AppScope
class PatientFaker @Inject constructor(private val faker: Faker) {

  fun patientPayload(
      fullName: String = faker.name.name(),
      gender: Gender = Gender.values().toList().shuffled().first(),
      age: Int? = Math.random().times(100).toInt(),
      ageUpdatedAt: Instant? = Instant.now(),
      status: PatientStatus = PatientStatus.values().toList().shuffled().first(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      address: PatientAddressPayload = addressPayload(),
      phoneNumber: PatientPhoneNumberPayload = phoneNumberPayload()
  ): PatientPayload {
    return PatientPayload(
        uuid = UUID.randomUUID(),
        fullName = fullName,
        gender = gender,
        dateOfBirth = null,
        age = age,
        ageUpdatedAt = ageUpdatedAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        address = address,
        phoneNumbers = listOf(phoneNumber)
    )
  }

  fun addressPayload(): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = UUID.randomUUID(),
        colonyOrVillage = faker.address.streetAddress(),
        district = faker.address.city(),
        state = faker.address.state(),
        country = faker.address.country(),
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun phoneNumberPayload(): PatientPhoneNumberPayload {
    return PatientPhoneNumberPayload(
        uuid = UUID.randomUUID(),
        number = faker.phoneNumber.phoneNumber(),
        type = PatientPhoneNumberType.values().toList().shuffled().first(),
        active = true,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun facility(
      uuid: UUID = TestClinicApp.qaUserFacilityUuid(),
      name: String = faker.company.name(),
      district: String = faker.address.city(),
      state: String = faker.address.state()
  ): Facility {
    return Facility(
        uuid = uuid,
        name = name,
        district = district,
        state = state,
        facilityType = null,
        streetAddress = null,
        villageOrColony = null,
        country = faker.address.country(),
        pinCode = null,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        syncStatus = SyncStatus.values().toList().shuffled().first())
  }

  fun loggedInUser(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.name.name(),
      phone: String = faker.phoneNumber.phoneNumber(),
      pinDigest: String = "pin-digest",
      status: UserStatus = UserStatus.values().toList().shuffled().first()
  ): LoggedInUser {
    return LoggedInUser(
        uuid = uuid,
        fullName = name,
        phoneNumber = phone,
        pinDigest = pinDigest,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun ongoingRegistrationEntry(facilities: List<Facility>): OngoingRegistrationEntry {
    val pin = faker.number.number(4)
    return OngoingRegistrationEntry(
        uuid = UUID.randomUUID(),
        phoneNumber = faker.number.number(10),
        fullName = faker.name.name(),
        pin = pin,
        pinConfirmation = pin,
        facilityIds = facilities.map { it.uuid },
        createdAt = Instant.now())
  }
}
