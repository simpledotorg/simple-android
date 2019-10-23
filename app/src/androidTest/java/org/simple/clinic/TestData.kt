package org.simple.clinic

import io.bloco.faker.Faker
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.encounter.Encounter
import org.simple.clinic.encounter.sync.EncounterObservationsPayload
import org.simple.clinic.encounter.sync.EncounterPayload
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.location.Coordinates
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentPayload
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion.BpPassportMetaDataV1
import org.simple.clinic.patient.businessid.BusinessIdMetaData
import org.simple.clinic.patient.businessid.BusinessIdMetaDataAdapter
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.sync.BusinessIdPayload
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.protocol.Protocol
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.protocol.sync.ProtocolDrugPayload
import org.simple.clinic.protocol.sync.ProtocolPayload
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.generateEncounterUuid
import org.simple.clinic.util.randomGender
import org.simple.clinic.util.randomMedicalHistoryAnswer
import org.simple.clinic.util.randomPatientPhoneNumberType
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass

private fun <T : Enum<T>> randomOfEnum(enumClass: KClass<T>): T {
  return enumClass.java.enumConstants!!.asList().shuffled().first()
}

@AppScope
class TestData @Inject constructor(
    private val faker: Faker,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val businessIdMetaDataAdapter: BusinessIdMetaDataAdapter,
    private val userClock: TestUserClock
) {

  fun qaUser() = userSession.loggedInUserImmediate()!!

  fun qaUserUuid() = qaUser().uuid

  fun qaUserPin() = "1712"

  fun qaUserOtp() = "000000"

  fun qaFacility() = facilityRepository.currentFacility(qaUser()).blockingFirst()

  fun qaUserFacilityUuid() = qaFacility().uuid

  fun patientProfile(
      patientUuid: UUID = UUID.randomUUID(),
      patientAddressUuid: UUID = UUID.randomUUID(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      generatePhoneNumber: Boolean = faker.bool.bool(),
      generateBusinessId: Boolean = faker.bool.bool(),
      patientStatus: PatientStatus = PatientStatus.Active,
      patientDeletedAt: Instant? = null,
      patientName: String = faker.name.name(),
      patientPhoneNumber: String? = if (generatePhoneNumber) faker.phoneNumber.phoneNumber() else null,
      businessId: BusinessId? = if (generateBusinessId) businessId(patientUuid = patientUuid) else null
  ): PatientProfile {
    val phoneNumbers = if (!patientPhoneNumber.isNullOrBlank()) {
      listOf(patientPhoneNumber(patientUuid = patientUuid, number = patientPhoneNumber))
    } else {
      emptyList()
    }
    val businessIds = if (businessId != null) {
      listOf(businessId)
    } else {
      emptyList()
    }

    return PatientProfile(
        patient = patient(
            uuid = patientUuid,
            fullName = patientName,
            syncStatus = syncStatus,
            addressUuid = patientAddressUuid,
            status = patientStatus,
            deletedAt = patientDeletedAt
        ),
        address = patientAddress(uuid = patientAddressUuid),
        phoneNumbers = phoneNumbers,
        businessIds = businessIds)
  }

  fun patient(
      uuid: UUID = UUID.randomUUID(),
      addressUuid: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      gender: Gender = randomGender(),
      dateOfBirth: LocalDate? = LocalDate.now(),
      age: Age? = Age(value = Math.random().times(100).toInt(), updatedAt = Instant.now()),
      status: PatientStatus = PatientStatus.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = addressUuid,
        fullName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        age = age,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt,
        syncStatus = syncStatus,
        reminderConsent = Granted
    )
  }

  fun patientPhoneNumber(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      number: String = faker.phoneNumber.phoneNumber(),
      phoneType: PatientPhoneNumberType = randomPatientPhoneNumberType(),
      active: Boolean = faker.bool.bool(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ) = PatientPhoneNumber(
      uuid = uuid,
      patientUuid = patientUuid,
      number = number,
      phoneType = phoneType,
      active = active,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt
  )

  fun patientAddress(
      uuid: UUID = UUID.randomUUID(),
      colonyOrVilage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      country: String? = faker.address.country(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ) = PatientAddress(
      uuid = uuid,
      colonyOrVillage = colonyOrVilage,
      district = district,
      state = state,
      country = country,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt
  )

  fun businessIdMetadata(
      metaData: BusinessIdMetaData = BusinessIdMetaData.BpPassportMetaDataV1(qaUserUuid(), qaUserFacilityUuid()),
      metaDataVersion: MetaDataVersion = BpPassportMetaDataV1
  ): String = businessIdMetaDataAdapter.serialize(metaData, metaDataVersion)

  fun businessId(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      identifier: Identifier = Identifier(value = UUID.randomUUID().toString(), type = Identifier.IdentifierType.BpPassport),
      meta: String = businessIdMetadata(
          metaData = BusinessIdMetaData.BpPassportMetaDataV1(
              assigningUserUuid = qaUserUuid(),
              assigningFacilityUuid = qaUserFacilityUuid()
          ),
          metaDataVersion = BpPassportMetaDataV1
      ),
      metaDataVersion: MetaDataVersion = BpPassportMetaDataV1,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ) = BusinessId(
      uuid = uuid,
      patientUuid = patientUuid,
      identifier = identifier,
      metaData = meta,
      metaDataVersion = metaDataVersion,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt
  )

  fun patientPayload(
      uuid: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      gender: Gender = randomGender(),
      age: Int? = Math.random().times(100).toInt(),
      dateOfBirth: LocalDate? = null,
      ageUpdatedAt: Instant? = Instant.now(),
      status: PatientStatus = PatientStatus.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now(),
      address: PatientAddressPayload = addressPayload(),
      phoneNumbers: List<PatientPhoneNumberPayload>? = listOf(phoneNumberPayload()),
      businessIds: List<BusinessIdPayload> = listOf(businessIdPayload())
  ): PatientPayload {
    return PatientPayload(
        uuid = uuid,
        fullName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        age = age,
        ageUpdatedAt = ageUpdatedAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        address = address,
        phoneNumbers = phoneNumbers,
        businessIds = businessIds,
        recordedAt = recordedAt,
        reminderConsent = Granted
    )
  }

  fun addressPayload(
      uuid: UUID = UUID.randomUUID(),
      colonyOrVillage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      country: String? = faker.address.country(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        district = district,
        state = state,
        country = country,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
  }

  fun phoneNumberPayload(
      uuid: UUID = UUID.randomUUID(),
      number: String = faker.phoneNumber.phoneNumber(),
      type: PatientPhoneNumberType = randomPatientPhoneNumberType(),
      active: Boolean = true,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): PatientPhoneNumberPayload {
    return PatientPhoneNumberPayload(
        uuid = uuid,
        number = number,
        type = type,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun businessIdPayload(
      uuid: UUID = UUID.randomUUID(),
      identifier: String = UUID.randomUUID().toString(),
      identifierType: Identifier.IdentifierType = Identifier.IdentifierType.BpPassport,
      metaDataVersion: MetaDataVersion = BpPassportMetaDataV1,
      meta: String = businessIdMetadata(
          metaData = BusinessIdMetaData.BpPassportMetaDataV1(
              assigningUserUuid = qaUserUuid(),
              assigningFacilityUuid = qaUserFacilityUuid()
          )
      ),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): BusinessIdPayload {
    return BusinessIdPayload(
        uuid = uuid,
        identifier = identifier,
        identifierType = identifierType,
        metaDataVersion = metaDataVersion,
        metaData = meta,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
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
      state: String = faker.address.state(),
      facilityType: String? = null,
      streetAddress: String? = null,
      villageOrColony: String? = null,
      country: String = faker.address.country(),
      pinCode: String? = null,
      protocolUuid: UUID? = UUID.randomUUID(),
      groupUuid: UUID? = UUID.randomUUID(),
      location: Coordinates? = Coordinates(
          latitude = faker.number.between(1.908537, 59.299800),
          longitude = faker.number.between(73.537524, 18.209118)),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      deletedAt: Instant? = null
  ): Facility {
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
        protocolUuid = protocolUuid,
        groupUuid = groupUuid,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        deletedAt = deletedAt)
  }

  fun facilityPayload(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.company.name(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      protocolUuid: UUID = UUID.randomUUID(),
      groupUuid: UUID = UUID.randomUUID(),
      facilityType: String? = null,
      streetAddress: String? = null,
      villageOrColony: String? = null,
      country: String = faker.address.country(),
      pinCode: String? = null,
      locationLatitude: Double? = faker.number.between(1.908537, 59.299800),
      locationLongitude: Double? = faker.number.between(73.537524, 18.209118),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): FacilityPayload {
    return FacilityPayload(
        uuid = uuid,
        name = name,
        facilityType = facilityType,
        streetAddress = streetAddress,
        villageOrColony = villageOrColony,
        district = district,
        state = state,
        country = country,
        pinCode = pinCode,
        protocolUuid = protocolUuid,
        groupUuid = groupUuid,
        locationLatitude = locationLatitude,
        locationLongitude = locationLongitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun loggedInUser(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.name.name(),
      phone: String = faker.phoneNumber.phoneNumber(),
      pinDigest: String = "pin-digest",
      status: UserStatus = UserStatus.random(),
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
      uuid: UUID = UUID.randomUUID(),
      phoneNumber: String = faker.number.number(10),
      pin: String = qaUserPin(),
      registrationFacility: Facility
  ): OngoingRegistrationEntry {
    return OngoingRegistrationEntry(
        uuid = uuid,
        phoneNumber = phoneNumber,
        fullName = faker.name.name(),
        pin = pin,
        pinConfirmation = pin,
        facilityId = registrationFacility.uuid,
        createdAt = Instant.now())
  }

  fun bpPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = qaUserFacilityUuid(),
      systolic: Int = faker.number.between(0, 299),
      diastolic: Int = faker.number.between(50, 60),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ): BloodPressureMeasurementPayload {
    return BloodPressureMeasurementPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        systolic = systolic,
        diastolic = diastolic,
        facilityUuid = facilityUuid,
        userUuid = qaUserUuid(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  fun prescription(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.food.dish(),
      dosage: String = "${faker.number.positive(10, 50)}mg",
      isProtocolDrug: Boolean = false,
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.randomUUID(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): PrescribedDrug {
    return PrescribedDrug(
        uuid = uuid,
        name = name,
        dosage = dosage,
        rxNormCode = "rx-norm-code",
        isDeleted = false,
        isProtocolDrug = isProtocolDrug,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
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
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
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
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun appointment(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = qaUserFacilityUuid(),
      scheduledDate: LocalDate = LocalDate.now(UTC).plusDays(30),
      status: Appointment.Status = Appointment.Status.random(),
      cancelReason: AppointmentCancelReason? = AppointmentCancelReason.random(),
      remindOn: LocalDate? = null,
      agreedToVisit: Boolean? = null,
      appointmentType: Appointment.AppointmentType = Appointment.AppointmentType.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): Appointment {
    return Appointment(
        uuid = uuid,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        scheduledDate = scheduledDate,
        status = status,
        cancelReason = cancelReason,
        remindOn = remindOn,
        agreedToVisit = agreedToVisit,
        appointmentType = appointmentType,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun appointmentPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      date: LocalDate = LocalDate.now(UTC).plusDays(30),
      facilityUuid: UUID = qaUserFacilityUuid(),
      status: Appointment.Status = Appointment.Status.random(),
      cancelReason: AppointmentCancelReason = AppointmentCancelReason.random(),
      remindOn: LocalDate? = null,
      agreedToVisit: Boolean? = null,
      appointmentType: Appointment.AppointmentType = Appointment.AppointmentType.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): AppointmentPayload {
    return AppointmentPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        date = date,
        status = status,
        cancelReason = cancelReason,
        remindOn = remindOn,
        agreedToVisit = agreedToVisit,
        appointmentType = appointmentType,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun medicalHistory(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      hasHadHeartAttack: Answer = randomMedicalHistoryAnswer(),
      hasHadStroke: Answer = randomMedicalHistoryAnswer(),
      hasHadKidneyDisease: Answer = randomMedicalHistoryAnswer(),
      diagnosedWithHypertension: Answer = randomMedicalHistoryAnswer(),
      isOnTreatmentForHypertension: Answer = randomMedicalHistoryAnswer(),
      hasDiabetes: Answer = randomMedicalHistoryAnswer(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): MedicalHistory {
    return MedicalHistory(
        uuid = uuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = diagnosedWithHypertension,
        isOnTreatmentForHypertension = isOnTreatmentForHypertension,
        hasHadHeartAttack = hasHadHeartAttack,
        hasHadStroke = hasHadStroke,
        hasHadKidneyDisease = hasHadKidneyDisease,
        hasDiabetes = hasDiabetes,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun medicalHistoryPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      diagnosedWithHypertension: Answer = randomMedicalHistoryAnswer(),
      hasHadHeartAttack: Answer = randomMedicalHistoryAnswer(),
      hasHadStroke: Answer = randomMedicalHistoryAnswer(),
      hasHadKidneyDisease: Answer = randomMedicalHistoryAnswer(),
      isOnTreatmentForHypertension: Answer = randomMedicalHistoryAnswer(),
      hasDiabetes: Answer = randomMedicalHistoryAnswer(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): MedicalHistoryPayload {
    return MedicalHistoryPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = diagnosedWithHypertension,
        isOnTreatmentForHypertension = isOnTreatmentForHypertension,
        hasHadHeartAttack = hasHadHeartAttack,
        hasHadStroke = hasHadStroke,
        hasHadKidneyDisease = hasHadKidneyDisease,
        hasDiabetes = hasDiabetes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun ongoingPatientEntry(
      fullName: String = faker.name.name(),
      dateOfBirth: String? = null,
      age: String? = faker.number.between(0, 100).toString(),
      gender: Gender = randomGender(),
      colony: String = faker.address.streetName(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      phone: String? = faker.number.number(10),
      identifier: Identifier? = null
  ): OngoingNewPatientEntry {
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails(fullName, dateOfBirth, age, gender)
    val ongoingAddress = OngoingNewPatientEntry.Address(colony, district, state)
    val ongoingPhoneNumber = phone?.let {
      OngoingNewPatientEntry.PhoneNumber(phone, PatientPhoneNumberType.Mobile, active = true)
    }

    return OngoingNewPatientEntry(
        personalDetails = ongoingPersonalDetails,
        address = ongoingAddress,
        phoneNumber = ongoingPhoneNumber,
        identifier = identifier
    )
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
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = uuid,
        systolic = systolic,
        diastolic = diastolic,
        syncStatus = syncStatus,
        userUuid = userUuid,
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt,
        encounterUuid = generateEncounterUuid(facilityUuid, patientUuid, recordedAt.toLocalDateAtZone(userClock.zone))
    )
  }

  fun protocol(
      uuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      name: String = "Protocol-Punjab",
      followUpDays: Int = 0,
      deletedAt: Instant? = null,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): Protocol {
    return Protocol(
        uuid = uuid,
        name = name,
        followUpDays = followUpDays,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = syncStatus
    )
  }

  fun protocolDrug(
      uuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      protocolUuid: UUID = UUID.randomUUID(),
      rxNormCode: String = "rx-NormCode-1",
      dosage: String = "20mg",
      name: String = "Amlodipine",
      deletedAt: Instant? = null,
      order: Int = 0
  ): ProtocolDrug {
    return ProtocolDrug(
        uuid = uuid,
        rxNormCode = rxNormCode,
        dosage = dosage,
        name = name,
        protocolUuid = protocolUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        order = order
    )
  }

  fun protocolPayload(
      uuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      name: String = "Punjab Protocol",
      followUpDays: Int = 0,
      protocolDrugs: List<ProtocolDrugPayload> = listOf(protocolDrugPayload(protocolUuid = uuid))
  ) = ProtocolPayload(
      uuid = uuid,
      createdAt = createdAt,
      updatedAt = updatedAt,
      name = name,
      followUpDays = followUpDays,
      protocolDrugs = protocolDrugs,
      deletedAt = deletedAt)

  fun protocolDrugPayload(
      uuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      protocolUuid: UUID = UUID.randomUUID(),
      rxNormCode: String = "rx-normcode",
      dosage: String = "5mg",
      name: String = "Amlodipine"
  ) = ProtocolDrugPayload(
      uuid = uuid,
      createdAt = createdAt,
      updatedAt = updatedAt,
      protocolUuid = protocolUuid,
      rxNormCode = rxNormCode,
      dosage = dosage,
      name = name,
      deletedAt = deletedAt)

  fun identifier(
      value: String = "identifier",
      type: Identifier.IdentifierType = Identifier.IdentifierType.random()
  ): Identifier {
    return Identifier(
        value = value,
        type = type
    )
  }

  fun encounter(
      uuid: UUID,
      patientUuid: UUID,
      facilityUuid: UUID? = null,
      encounteredOn: LocalDate = LocalDate.now(),
      syncStatus: SyncStatus = SyncStatus.DONE,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): Encounter {
    return Encounter(
        uuid = if (facilityUuid == null) uuid else generateEncounterUuid(facilityUuid, patientUuid, encounteredOn),
        patientUuid = patientUuid,
        encounteredOn = encounteredOn,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
  }

  fun encounterPayload(
      uuid: UUID,
      patientUuid: UUID,
      encounteredOn: LocalDate = LocalDate.now(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      bpPayloads: List<BloodPressureMeasurementPayload> = listOf(bpPayload(patientUuid = patientUuid))
  ): EncounterPayload {
    return EncounterPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        encounteredOn = encounteredOn,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        observations = EncounterObservationsPayload(bloodPressureMeasurements = bpPayloads)
    )
  }
}
