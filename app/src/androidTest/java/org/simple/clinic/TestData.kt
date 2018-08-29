package org.simple.clinic

import io.bloco.faker.Faker
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass

private fun <T : Enum<T>> randomOfEnum(enumClass: KClass<T>): T {
  return enumClass.java.enumConstants.asList().shuffled().first()
}

@AppScope
class TestData @Inject constructor(private val faker: Faker) {

  fun patientPayload(
      fullName: String = faker.name.name(),
      gender: Gender = randomOfEnum(Gender::class),
      age: Int? = Math.random().times(100).toInt(),
      ageUpdatedAt: Instant? = Instant.now(),
      status: PatientStatus = randomOfEnum(PatientStatus::class),
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
        type = randomOfEnum(PatientPhoneNumberType::class),
        active = true,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  /**
   * [uuid] is not optional because dummy facility IDs should never be sent to
   * the server. Doing so may result in data loss due to foreign key constraints.
   */
  fun facility(
      uuid: UUID,
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
        syncStatus = randomOfEnum(SyncStatus::class))
  }

  fun loggedInUser(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.name.name(),
      phone: String = faker.phoneNumber.phoneNumber(),
      pinDigest: String = "pin-digest",
      status: UserStatus = randomOfEnum(UserStatus::class),
      loggedInStatus: User.LoggedInStatus = randomOfEnum(User.LoggedInStatus::class)
  ): User {
    return User(
        uuid = uuid,
        fullName = name,
        phoneNumber = phone,
        pinDigest = pinDigest,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        loggedInStatus = loggedInStatus)
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

  fun bpPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      systolic: Int = faker.number.between(0, 299),
      diastolic: Int = faker.number.between(50, 60)
  ): BloodPressureMeasurementPayload {
    return BloodPressureMeasurementPayload(
        uuid = uuid,
        systolic = systolic,
        diastolic = diastolic,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        userUuid = TestClinicApp.qaUserUuid(),
        facilityUuid = TestClinicApp.qaUserFacilityUuid(),
        patientUuid = patientUuid)
  }
}
