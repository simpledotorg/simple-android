package org.simple.clinic

import io.bloco.faker.Faker
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentPayload
import org.simple.clinic.overdue.communication.Communication
import org.simple.clinic.overdue.communication.CommunicationPayload
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass

private fun <T : Enum<T>> randomOfEnum(enumClass: KClass<T>): T {
  return enumClass.java.enumConstants.asList().shuffled().first()
}

@AppScope
class TestData @Inject constructor(private val faker: Faker) {

  fun qaUserUuid() = UUID.fromString("c6834f82-3305-4144-9dc8-5f77c908ebf1")

  fun qaUserOtp(): String = "000000"

  fun qaOngoingLoginEntry() = OngoingLoginEntry(qaUserUuid(), phoneNumber = "0000", pin = "0000")

  @Deprecated(message = "Get real facilities from the server instead. Look at UserSessionAndroidTest for examples.")
  fun qaUserFacilityUuid(): UUID {
    return UUID.fromString("43dad34c-139e-4e5f-976e-a3ef1d9ac977")
  }

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
        userUuid = qaUserUuid(),
        facilityUuid = qaUserFacilityUuid(),
        patientUuid = patientUuid)
  }

  fun appointment(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): Appointment {
    return Appointment(
        uuid = UUID.randomUUID(),
        patientUuid = UUID.randomUUID(),
        date = LocalDate.now(UTC).plusDays(30),
        facilityUuid = qaUserFacilityUuid(),
        status = randomOfEnum(Appointment.Status::class),
        statusReason = randomOfEnum(Appointment.StatusReason::class),
        syncStatus = syncStatus,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun appointmentPayload(): AppointmentPayload {
    return AppointmentPayload(
        uuid = UUID.randomUUID(),
        patientUuid = UUID.randomUUID(),
        date = LocalDate.now(UTC).plusDays(30),
        facilityUuid = qaUserFacilityUuid(),
        status = randomOfEnum(Appointment.Status::class),
        statusReason = randomOfEnum(Appointment.StatusReason::class),
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun communication(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): Communication {
    return Communication(
        uuid = UUID.randomUUID(),
        appointmentUuid = UUID.randomUUID(),
        userUuid = qaUserUuid(),
        type = randomOfEnum(Communication.Type::class),
        result = randomOfEnum(Communication.Result::class),
        syncStatus = syncStatus,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun communicationPayload(): CommunicationPayload {
    return CommunicationPayload(
        uuid = UUID.randomUUID(),
        appointmentUuid = UUID.randomUUID(),
        userUuid = qaUserUuid(),
        type = randomOfEnum(Communication.Type::class),
        result = randomOfEnum(Communication.Result::class),
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun medicalHistory(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): MedicalHistory {
    return MedicalHistory(
        uuid = UUID.randomUUID(),
        patientUuid = UUID.randomUUID(),
        hasHadHeartAttack = faker.bool.bool(),
        hasHadStroke = faker.bool.bool(),
        hasHadKidneyDisease = faker.bool.bool(),
        isOnTreatmentForHypertension = faker.bool.bool(),
        hasDiabetes = faker.bool.bool(),
        syncStatus = syncStatus,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun medicalHistoryPayload(): MedicalHistoryPayload {
    return MedicalHistoryPayload(
        uuid = UUID.randomUUID(),
        patientUuid = UUID.randomUUID(),
        hasHadHeartAttack = faker.bool.bool(),
        hasHadStroke = faker.bool.bool(),
        hasHadKidneyDisease = faker.bool.bool(),
        isOnTreatmentForHypertension = faker.bool.bool(),
        hasDiabetes = faker.bool.bool(),
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }
}
