package org.simple.clinic

import io.bloco.faker.Faker
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentPayload
import org.simple.clinic.overdue.communication.Communication
import org.simple.clinic.overdue.communication.CommunicationPayload
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.nameToSearchableForm
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
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
class TestData @Inject constructor(
    private val faker: Faker,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession
) {

  fun qaUserUuid(): UUID =
      userSession.requireLoggedInUser()
          .map { it.uuid }
          .blockingFirst()

  fun qaUserPin() = "1712"

  fun qaUserOtp() = "000000"

  fun qaUserFacilityUuid(): UUID =
      facilityRepository.currentFacility(userSession)
          .map { it.uuid }
          .blockingFirst()

  fun patientProfile(
      patientUuid: UUID = UUID.randomUUID(),
      patientAddressUuid: UUID = UUID.randomUUID(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): PatientProfile {
    return PatientProfile(
        patient = patient(uuid = patientUuid, syncStatus = syncStatus, addressUuid = patientAddressUuid),
        address = patientAddress(uuid = patientAddressUuid),
        phoneNumbers = (0 until Math.random().times(10).toInt()).map { patientPhoneNumber(patientUuid = patientUuid) }
    )
  }

  fun patient(
      uuid: UUID = UUID.randomUUID(),
      addressUuid: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      searchableName: String = nameToSearchableForm(fullName),
      gender: Gender = randomOfEnum(Gender::class),
      dateOfBirth: LocalDate? = LocalDate.now(),
      age: Age? = Age(value = Math.random().times(100).toInt(), updatedAt = Instant.now(), computedDateOfBirth = LocalDate.now()),
      status: PatientStatus = randomOfEnum(PatientStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = addressUuid,
        fullName = fullName,
        searchableName = searchableName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        age = age,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
  }

  fun patientPhoneNumber(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      number: String = faker.phoneNumber.phoneNumber(),
      phoneType: PatientPhoneNumberType = randomOfEnum(PatientPhoneNumberType::class),
      active: Boolean = faker.bool.bool(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ) = PatientPhoneNumber(
      uuid = uuid,
      patientUuid = patientUuid,
      number = number,
      phoneType = phoneType,
      active = active,
      createdAt = createdAt,
      updatedAt = updatedAt
  )

  fun patientAddress(
      uuid: UUID = UUID.randomUUID(),
      colonyOrVilage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      country: String? = faker.address.country(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ) = PatientAddress(
      uuid = uuid,
      colonyOrVillage = colonyOrVilage,
      district = district,
      state = state,
      country = country,
      createdAt = createdAt,
      updatedAt = updatedAt
  )

  fun patientPayload(
      uuid: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      gender: Gender = randomOfEnum(Gender::class),
      age: Int? = Math.random().times(100).toInt(),
      ageUpdatedAt: Instant? = Instant.now(),
      status: PatientStatus = randomOfEnum(PatientStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      address: PatientAddressPayload = addressPayload(),
      phoneNumbers: List<PatientPhoneNumberPayload>? = listOf(phoneNumberPayload())
  ): PatientPayload {
    return PatientPayload(
        uuid = uuid,
        fullName = fullName,
        gender = gender,
        dateOfBirth = null,
        age = age,
        ageUpdatedAt = ageUpdatedAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        address = address,
        phoneNumbers = phoneNumbers
    )
  }

  fun addressPayload(
      uuid: UUID = UUID.randomUUID(),
      colonyOrVillage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      country: String? = faker.address.country(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        district = district,
        state = state,
        country = country,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
  }

  fun phoneNumberPayload(
      uuid: UUID = UUID.randomUUID(),
      number: String = faker.phoneNumber.phoneNumber(),
      type: PatientPhoneNumberType = randomOfEnum(PatientPhoneNumberType::class),
      active: Boolean = true,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): PatientPhoneNumberPayload {
    return PatientPhoneNumberPayload(
        uuid = uuid,
        number = number,
        type = type,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
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

  fun facilityPayload(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.company.name(),
      district: String = faker.address.city(),
      state: String = faker.address.state()
  ): FacilityPayload {
    return FacilityPayload(
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
        updatedAt = Instant.now()
    )
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

  fun ongoingRegistrationEntry(
      phoneNumber: String = faker.number.number(10),
      pin: String = qaUserPin(),
      facilities: List<Facility>
  ): OngoingRegistrationEntry {
    return OngoingRegistrationEntry(
        uuid = UUID.randomUUID(),
        phoneNumber = phoneNumber,
        fullName = faker.name.name(),
        pin = pin,
        pinConfirmation = pin,
        facilityIds = facilities.map { it.uuid },
        createdAt = Instant.now())
  }

  fun bpPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilytyUuid: UUID = qaUserFacilityUuid(),
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
        facilityUuid = facilytyUuid,
        patientUuid = patientUuid)
  }

  fun prescriptionPayload(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.food.dish(),
      dosage: String = "${faker.number.positive(10, 50)}mg",
      rxNormCode: String = faker.food.metricMeasurement(),
      isDeleted: Boolean = false,
      isProtocolDrug: Boolean = false,
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): PrescribedDrugPayload {
    return PrescribedDrugPayload(
        uuid = uuid,
        name = name,
        dosage = dosage,
        rxNormCode = rxNormCode,
        isDeleted = isDeleted,
        isProtocolDrug = isProtocolDrug,
        patientId = patientUuid,
        facilityId = facilityUuid,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
  }

  fun appointment(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): Appointment {
    return Appointment(
        uuid = UUID.randomUUID(),
        patientUuid = UUID.randomUUID(),
        scheduledDate = LocalDate.now(UTC).plusDays(30),
        facilityUuid = qaUserFacilityUuid(),
        status = randomOfEnum(Appointment.Status::class),
        cancelReason = randomOfEnum(Appointment.CancelReason::class),
        remindOn = null,
        agreedToVisit = null,
        syncStatus = syncStatus,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun appointmentPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      date: LocalDate = LocalDate.now(UTC).plusDays(30),
      facilityUuid: UUID = qaUserFacilityUuid(),
      status: Appointment.Status = randomOfEnum(Appointment.Status::class),
      cancelReason: Appointment.CancelReason = randomOfEnum(Appointment.CancelReason::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): AppointmentPayload {
    return AppointmentPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        date = date,
        facilityUuid = facilityUuid,
        status = status,
        cancelReason = cancelReason,
        remindOn = null,
        agreedToVisit = null,
        createdAt = createdAt,
        updatedAt = updatedAt)
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

  fun communicationPayload(
      uuid: UUID = UUID.randomUUID(),
      appointmentUuid: UUID = UUID.randomUUID(),
      userUuid: UUID = qaUserUuid(),
      type: Communication.Type = randomOfEnum(Communication.Type::class),
      result: Communication.Result = randomOfEnum(Communication.Result::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): CommunicationPayload {
    return CommunicationPayload(
        uuid = uuid,
        appointmentUuid = appointmentUuid,
        userUuid = userUuid,
        type = type,
        result = result,
        createdAt = createdAt,
        updatedAt = updatedAt)
  }

  fun medicalHistory(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      hasHadHeartAttack: Boolean = faker.bool.bool(),
      hasHadStroke: Boolean = faker.bool.bool(),
      hasHadKidneyDisease: Boolean = faker.bool.bool(),
      diagnosedWithHypertension: Boolean = faker.bool.bool(),
      isOnTreatmentForHypertension: Boolean = faker.bool.bool(),
      hasDiabetes: Boolean = faker.bool.bool(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): MedicalHistory {
    return MedicalHistory(
        uuid = uuid,
        patientUuid = patientUuid,
        hasHadHeartAttack = hasHadHeartAttack,
        hasHadStroke = hasHadStroke,
        hasHadKidneyDisease = hasHadKidneyDisease,
        diagnosedWithHypertension = diagnosedWithHypertension,
        isOnTreatmentForHypertension = isOnTreatmentForHypertension,
        hasDiabetes = hasDiabetes,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt)
  }

  fun medicalHistoryPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      hasHadHeartAttack: Boolean = faker.bool.bool(),
      hasHadStroke: Boolean = faker.bool.bool(),
      hasHadKidneyDisease: Boolean = faker.bool.bool(),
      isOnTreatmentForHypertension: Boolean = faker.bool.bool(),
      hasDiabetes: Boolean = faker.bool.bool(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): MedicalHistoryPayload {
    return MedicalHistoryPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        hasHadHeartAttack = hasHadHeartAttack,
        hasHadStroke = hasHadStroke,
        hasHadKidneyDisease = hasHadKidneyDisease,
        isOnTreatmentForHypertension = isOnTreatmentForHypertension,
        hasDiabetes = hasDiabetes,
        createdAt = createdAt,
        updatedAt = updatedAt)
  }

  fun ongoingPatientEntry(
      fullName: String = faker.name.name(),
      dateOfBirth: String? = null,
      age: String? = faker.number.between(0, 100).toString(),
      gender: Gender = randomOfEnum(Gender::class),
      colony: String = faker.address.streetName(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      phone: String? = faker.number.number(10)
  ): OngoingPatientEntry {
    val ongoingPersonalDetails = OngoingPatientEntry.PersonalDetails(fullName, dateOfBirth, age, gender)
    val ongoingAddress = OngoingPatientEntry.Address(colony, district, state)
    val ongoingPhoneNumber = phone?.let {
      OngoingPatientEntry.PhoneNumber(phone, PatientPhoneNumberType.MOBILE, active = true)
    }

    return OngoingPatientEntry(ongoingPersonalDetails, ongoingAddress, ongoingPhoneNumber)
  }

  fun bloodPressureMeasurement(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = qaUserFacilityUuid(),
      userUuid: UUID = qaUserUuid(),
      systolic: Int = faker.number.between(0, 299),
      diastolic: Int = faker.number.between(50, 60),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = uuid,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        systolic = systolic,
        diastolic = diastolic,
        syncStatus = syncStatus,
        userUuid = userUuid,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
  }
}
