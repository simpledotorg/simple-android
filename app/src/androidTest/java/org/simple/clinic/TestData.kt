package org.simple.clinic

import android.speech.RecognizerResultsIntent
import io.bloco.faker.Faker
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.di.AppScope
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.location.Coordinates
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistory.Answer
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentPayload
import org.simple.clinic.overdue.communication.Communication
import org.simple.clinic.overdue.communication.CommunicationPayload
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion.BpPassportMetaDataV1
import org.simple.clinic.patient.businessid.BusinessIdMetaData
import org.simple.clinic.patient.businessid.BusinessIdMetaDataAdapter
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.nameToSearchableForm
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
    private val userSession: UserSession,
    private val businessIdMetaDataAdapter: BusinessIdMetaDataAdapter
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
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      generatePhoneNumber: Boolean = faker.bool.bool(),
      generateBusinessId: Boolean = faker.bool.bool()
  ): PatientProfile {
    val phoneNumbers = if (generatePhoneNumber) listOf(patientPhoneNumber(patientUuid = patientUuid)) else emptyList()
    val businessIds = if (generateBusinessId) listOf(businessId(patientUuid = patientUuid)) else emptyList()

    return PatientProfile(
        patient = patient(uuid = patientUuid, syncStatus = syncStatus, addressUuid = patientAddressUuid),
        address = patientAddress(uuid = patientAddressUuid),
        phoneNumbers = phoneNumbers,
        businessIds = businessIds)
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
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now(),
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
        deletedAt = deletedAt,
        recordedAt = recordedAt,
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
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ) = PatientPhoneNumber(
      uuid = uuid,
      patientUuid = patientUuid,
      number = number,
      phoneType = phoneType,
      active = active,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt,
      recordedAt = recordedAt
  )

  fun patientAddress(
      uuid: UUID = UUID.randomUUID(),
      colonyOrVilage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      country: String? = faker.address.country(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ) = PatientAddress(
      uuid = uuid,
      colonyOrVillage = colonyOrVilage,
      district = district,
      state = state,
      country = country,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt,
      recordedAt = recordedAt
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
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ) = BusinessId(
      uuid = uuid,
      patientUuid = patientUuid,
      identifier = identifier,
      metaData = meta,
      metaDataVersion = metaDataVersion,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt,
      recordedAt = recordedAt
  )

  fun patientPayload(
      uuid: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      gender: Gender = randomOfEnum(Gender::class),
      age: Int? = Math.random().times(100).toInt(),
      dateOfBirth: LocalDate? = null,
      ageUpdatedAt: Instant? = Instant.now(),
      status: PatientStatus = randomOfEnum(PatientStatus::class),
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
        recordedAt = recordedAt
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
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        district = district,
        state = state,
        country = country,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt
    )
  }

  fun phoneNumberPayload(
      uuid: UUID = UUID.randomUUID(),
      number: String = faker.phoneNumber.phoneNumber(),
      type: PatientPhoneNumberType = randomOfEnum(PatientPhoneNumberType::class),
      active: Boolean = true,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ): PatientPhoneNumberPayload {
    return PatientPhoneNumberPayload(
        uuid = uuid,
        number = number,
        type = type,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt)
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
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ): BusinessIdPayload {
    return BusinessIdPayload(
        uuid = uuid,
        identifier = identifier,
        identifierType = identifierType,
        metaDataVersion = metaDataVersion,
        metaData = meta,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt
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
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
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
        deletedAt = deletedAt,
        recordedAt = recordedAt)
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
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
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
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  fun appointment(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = qaUserFacilityUuid(),
      scheduledDate: LocalDate = LocalDate.now(UTC).plusDays(30),
      status: Appointment.Status = randomOfEnum(Appointment.Status::class),
      cancelReason: AppointmentCancelReason = AppointmentCancelReason.random(),
      remindOn: LocalDate? = null,
      agreedToVisit: Boolean? = null,
      appointmentType: Appointment.AppointmentType = Appointment.AppointmentType.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
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
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  fun appointmentPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      date: LocalDate = LocalDate.now(UTC).plusDays(30),
      facilityUuid: UUID = qaUserFacilityUuid(),
      status: Appointment.Status = randomOfEnum(Appointment.Status::class),
      cancelReason: AppointmentCancelReason = AppointmentCancelReason.random(),
      remindOn: LocalDate? = null,
      agreedToVisit: Boolean? = null,
      appointmentType: Appointment.AppointmentType = Appointment.AppointmentType.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
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
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  fun communication(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      uuid: UUID = UUID.randomUUID(),
      appointmentUuid: UUID = UUID.randomUUID(),
      userUuid: UUID = qaUserUuid(),
      type: Communication.Type = randomOfEnum(Communication.Type::class),
      result: Communication.Result = randomOfEnum(Communication.Result::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): Communication {
    return Communication(
        uuid = uuid,
        appointmentUuid = appointmentUuid,
        userUuid = userUuid,
        type = type,
        result = result,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun communicationPayload(
      uuid: UUID = UUID.randomUUID(),
      appointmentUuid: UUID = UUID.randomUUID(),
      userUuid: UUID = qaUserUuid(),
      type: Communication.Type = randomOfEnum(Communication.Type::class),
      result: Communication.Result = randomOfEnum(Communication.Result::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): CommunicationPayload {
    return CommunicationPayload(
        uuid = uuid,
        appointmentUuid = appointmentUuid,
        userUuid = userUuid,
        type = type,
        result = result,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun medicalHistory(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      hasHadHeartAttack: Answer = randomOfEnum(Answer::class),
      hasHadStroke: Answer = randomOfEnum(Answer::class),
      hasHadKidneyDisease: Answer = randomOfEnum(Answer::class),
      diagnosedWithHypertension: Answer = randomOfEnum(Answer::class),
      isOnTreatmentForHypertension: Answer = randomOfEnum(Answer::class),
      hasDiabetes: Answer = randomOfEnum(Answer::class),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
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
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  fun medicalHistoryPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      diagnosedWithHypertension: Answer = randomOfEnum(Answer::class),
      hasHadHeartAttack: Answer = randomOfEnum(Answer::class),
      hasHadStroke: Answer = randomOfEnum(Answer::class),
      hasHadKidneyDisease: Answer = randomOfEnum(Answer::class),
      isOnTreatmentForHypertension: Answer = randomOfEnum(Answer::class),
      hasDiabetes: Answer = randomOfEnum(Answer::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
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
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  fun ongoingPatientEntry(
      fullName: String = faker.name.name(),
      dateOfBirth: String? = null,
      age: String? = faker.number.between(0, 100).toString(),
      gender: Gender = randomOfEnum(Gender::class),
      colony: String = faker.address.streetName(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      phone: String? = faker.number.number(10),
      identifier: Identifier? = null
  ): OngoingNewPatientEntry {
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails(fullName, dateOfBirth, age, gender)
    val ongoingAddress = OngoingNewPatientEntry.Address(colony, district, state)
    val ongoingPhoneNumber = phone?.let {
      OngoingNewPatientEntry.PhoneNumber(phone, PatientPhoneNumberType.MOBILE, active = true)
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
        recordedAt = recordedAt
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
}
