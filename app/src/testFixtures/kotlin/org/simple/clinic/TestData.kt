package org.simple.clinic

import io.bloco.faker.Faker
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.appconfig.State
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.sync.BloodSugarMeasurementPayload
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.sync.BloodPressureMeasurementPayload
import org.simple.clinic.contactpatient.ContactPatientProfile
import org.simple.clinic.cvdrisk.AgeData
import org.simple.clinic.cvdrisk.CVDRisk
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.LabBasedCVDRiskCalculationSheet
import org.simple.clinic.cvdrisk.LabBasedRiskEntry
import org.simple.clinic.cvdrisk.Men
import org.simple.clinic.cvdrisk.NonLabBasedCVDRiskCalculationSheet
import org.simple.clinic.cvdrisk.NonLabBasedRiskEntry
import org.simple.clinic.cvdrisk.Women
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.search.Answer.Yes
import org.simple.clinic.drugs.search.Drug
import org.simple.clinic.drugs.search.DrugCategory
import org.simple.clinic.drugs.search.DrugCategory.Hypertension.CCB
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.OverduePatientAddress
import org.simple.clinic.location.Coordinates
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentPayload
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.Outcome
import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.DeletedReason
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientPrefillInfo
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.patient.ReminderConsent
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion.BpPassportMetaDataV1
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.sync.BusinessIdPayload
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.patientattribute.PatientAttribute
import org.simple.clinic.protocol.Protocol
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.protocol.sync.ProtocolDrugPayload
import org.simple.clinic.protocol.sync.ProtocolPayload
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.Questionnaire
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.questionnaire.component.HeaderComponentData
import org.simple.clinic.questionnaire.component.InputFieldComponentData
import org.simple.clinic.questionnaire.component.InputViewGroupComponentData
import org.simple.clinic.questionnaire.component.LineSeparatorComponentData
import org.simple.clinic.questionnaire.component.ParagraphComponentData
import org.simple.clinic.questionnaire.component.RadioButtonComponentData
import org.simple.clinic.questionnaire.component.RadioViewGroupComponentData
import org.simple.clinic.questionnaire.component.SeparatorComponentData
import org.simple.clinic.questionnaire.component.SubHeaderComponentData
import org.simple.clinic.questionnaire.component.UnorderedListItemComponentData
import org.simple.clinic.questionnaire.component.UnorderedListViewGroupComponentData
import org.simple.clinic.questionnaire.component.ViewGroupComponentData
import org.simple.clinic.questionnaire.component.properties.InputFieldValidations
import org.simple.clinic.questionnaire.component.properties.IntegerType
import org.simple.clinic.questionnaire.component.properties.StringType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.scanid.IndiaNHIDGender
import org.simple.clinic.scanid.IndiaNHIDInfoPayload
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficerPayload
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityInfo
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityInfoPayload
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityMedicalOfficersCrossRef
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityWithMedicalOfficers
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecord
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordInfo
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRequestInfo
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.randomDeletedReason
import org.simple.clinic.util.randomGender
import org.simple.clinic.util.randomMedicalHistoryAnswer
import org.simple.clinic.util.randomOfEnum
import org.simple.clinic.util.randomPatientPhoneNumberType
import org.simple.clinic.util.randomTeleconsultRecordAnswer
import org.simple.clinic.util.randomTeleconsultationType
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.util.UUID
import org.simple.clinic.drugs.search.Answer as DrugAnswer
import org.simple.clinic.patient.Answer as PatientAnswer
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer as TeleconsultRecordAnswer

object TestData {

  private val faker = Faker("en-IND")

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
      businessId: BusinessId? = if (generateBusinessId) businessId(patientUuid = patientUuid) else null,
      businessIds: List<BusinessId> = if (businessId != null) listOf(businessId) else emptyList(),
      generateDateOfBirth: Boolean = faker.bool.bool(),
      gender: Gender = randomGender(),
      patientDeletedReason: DeletedReason? = null,
      patientCreatedAt: Instant = Instant.now(),
      patientUpdatedAt: Instant = Instant.now(),
      patientRecordedAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      patientRegisteredFacilityId: UUID? = null,
      patientAssignedFacilityId: UUID? = null,
      retainUntil: Instant? = null,
      patientAgeDetails: PatientAgeDetails = PatientAgeDetails(
          ageValue = if (!generateDateOfBirth) Math.random().times(100).toInt() else null,
          ageUpdatedAt = if (!generateDateOfBirth) Instant.parse("2018-01-01T00:00:00Z") else null,
          dateOfBirth = if (generateDateOfBirth) LocalDate.parse("1980-01-01") else null
      ),
      patientAddressStreet: String = faker.address.streetName(),
      patientAddressColonyOrVillage: String = faker.address.streetAddress(),
      eligibleForReassignment: PatientAnswer = PatientAnswer.Unanswered,
  ): PatientProfile {
    val phoneNumbers = if (!patientPhoneNumber.isNullOrBlank()) {
      listOf(patientPhoneNumber(patientUuid = patientUuid, number = patientPhoneNumber, phoneType = PatientPhoneNumberType.Mobile))
    } else {
      emptyList()
    }

    return PatientProfile(
        patient = patient(
            uuid = patientUuid,
            addressUuid = patientAddressUuid,
            fullName = patientName,
            gender = gender,
            status = patientStatus,
            createdAt = patientCreatedAt,
            updatedAt = patientUpdatedAt,
            deletedAt = patientDeletedAt,
            recordedAt = patientRecordedAt,
            syncStatus = syncStatus,
            deletedReason = patientDeletedReason,
            registeredFacilityId = patientRegisteredFacilityId,
            assignedFacilityId = patientAssignedFacilityId,
            retainUntil = retainUntil,
            patientAgeDetails = patientAgeDetails,
            eligibleForReassignment = eligibleForReassignment
        ),
        address = patientAddress(
            uuid = patientAddressUuid,
            streetAddress = patientAddressStreet,
            colonyOrVillage = patientAddressColonyOrVillage
        ),
        phoneNumbers = phoneNumbers,
        businessIds = businessIds)
  }

  fun patient(
      uuid: UUID = UUID.randomUUID(),
      addressUuid: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      gender: Gender = randomGender(),
      status: PatientStatus = PatientStatus.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      reminderConsent: ReminderConsent = Granted,
      deletedReason: DeletedReason? = null,
      registeredFacilityId: UUID? = null,
      assignedFacilityId: UUID? = null,
      retainUntil: Instant? = null,
      patientAgeDetails: PatientAgeDetails = PatientAgeDetails(
          ageValue = Math.random().times(100).toInt(),
          ageUpdatedAt = Instant.now(),
          dateOfBirth = null
      ),
      eligibleForReassignment: PatientAnswer = PatientAnswer.Unanswered,
  ): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = addressUuid,
        fullName = fullName,
        gender = gender,
        ageDetails = patientAgeDetails,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt,
        syncStatus = syncStatus,
        reminderConsent = reminderConsent,
        deletedReason = deletedReason,
        registeredFacilityId = registeredFacilityId,
        assignedFacilityId = assignedFacilityId,
        retainUntil = retainUntil,
        eligibleForReassignment = eligibleForReassignment,
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
      streetAddress: String? = faker.address.streetName(),
      colonyOrVillage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      zone: String? = faker.address.secondaryAddress(),
      state: String = faker.address.state(),
      country: String? = faker.address.country(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ) = PatientAddress(
      uuid = uuid,
      streetAddress = streetAddress,
      colonyOrVillage = colonyOrVillage,
      zone = zone,
      district = district,
      state = state,
      country = country,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt
  )

  fun businessId(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      identifier: Identifier = Identifier(value = UUID.randomUUID().toString(), type = BpPassport),
      meta: String = "",
      metaDataVersion: MetaDataVersion = BpPassportMetaDataV1,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      identifierSearchHelp: String = ""
  ) = BusinessId(
      uuid = uuid,
      patientUuid = patientUuid,
      identifier = identifier,
      metaDataVersion = metaDataVersion,
      metaData = meta,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt,
      searchHelp = identifierSearchHelp
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
      businessIds: List<BusinessIdPayload> = listOf(businessIdPayload()),
      deletedReason: DeletedReason? = randomDeletedReason(),
      registeredFacilityId: UUID? = null,
      assignedFacilityId: UUID? = null,
      eligibleForReassignment: PatientAnswer = PatientAnswer.Yes
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
        reminderConsent = Granted,
        deletedReason = deletedReason,
        registeredFacilityId = registeredFacilityId,
        assignedFacilityId = assignedFacilityId,
        eligibleForReassignment = eligibleForReassignment
    )
  }

  fun addressPayload(
      uuid: UUID = UUID.randomUUID(),
      streetAddress: String? = faker.address.streetName(),
      colonyOrVillage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      zone: String? = faker.address.secondaryAddress(),
      state: String = faker.address.state(),
      country: String? = faker.address.country(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        streetAddress = streetAddress,
        district = district,
        zone = zone,
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
      identifierType: Identifier.IdentifierType = BpPassport,
      metaDataVersion: MetaDataVersion = BpPassportMetaDataV1,
      meta: String = "",
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
      uuid: UUID = UUID.randomUUID(),
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
      deletedAt: Instant? = null,
      facilityConfig: FacilityConfig = FacilityConfig(
          diabetesManagementEnabled = false,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false,
      ),
      syncGroup: String = ""
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
        deletedAt = deletedAt,
        config = facilityConfig,
        syncGroup = syncGroup
    )
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
      deletedAt: Instant? = null,
      facilityConfig: FacilityConfig = FacilityConfig(
          diabetesManagementEnabled = false,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false,
      ),
      syncGroup: String? = null
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
        deletedAt = deletedAt,
        config = facilityConfig,
        syncGroup = syncGroup
    )
  }

  fun loggedInUser(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.name.name(),
      phone: String = faker.phoneNumber.phoneNumber(),
      pinDigest: String = "pin-digest",
      status: UserStatus = UserStatus.random(),
      loggedInStatus: User.LoggedInStatus = randomOfEnum(User.LoggedInStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      registrationFacilityUuid: UUID = UUID.fromString("fa85410a-54ca-449c-b3d6-7caf9def1474"),
      currentFacilityUuid: UUID = registrationFacilityUuid,
      teleconsultPhoneNumber: String = "1111111111",
      capabilities: User.Capabilities? = null
  ): User {
    return User(
        uuid = uuid,
        fullName = name,
        phoneNumber = phone,
        pinDigest = pinDigest,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        loggedInStatus = loggedInStatus,
        registrationFacilityUuid = registrationFacilityUuid,
        currentFacilityUuid = currentFacilityUuid,
        teleconsultPhoneNumber = teleconsultPhoneNumber,
        capabilities = capabilities
    )
  }

  fun ongoingRegistrationEntry(
      uuid: UUID = UUID.randomUUID(),
      phoneNumber: String = faker.number.number(10),
      fullName: String? = faker.name.name(),
      pin: String = "1111",
      registrationFacility: Facility? = null
  ): OngoingRegistrationEntry {
    return OngoingRegistrationEntry(
        uuid = uuid,
        phoneNumber = phoneNumber,
        fullName = fullName,
        pin = pin,
        facilityId = registrationFacility?.uuid
    )
  }

  fun bpPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe"),
      systolic: Int = faker.number.between(0, 299),
      diastolic: Int = faker.number.between(50, 60),
      userUuid: UUID = UUID.fromString("4e3442df-ffa4-4a66-9d5f-672d3135c460"),
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
        userUuid = userUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        recordedAt = recordedAt)
  }

  fun bloodSugarPayload(
      uuid: UUID = UUID.randomUUID(),
      bloodSugarType: BloodSugarMeasurementType = BloodSugarMeasurementType.random(),
      bloodSugarValue: Float = faker.number.between(0, 300).toFloat(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe"),
      userUuid: UUID = UUID.fromString("4e3442df-ffa4-4a66-9d5f-672d3135c460"),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      recordedAt: Instant = Instant.now()
  ) = BloodSugarMeasurementPayload(
      uuid = uuid,
      bloodSugarType = bloodSugarType,
      bloodSugarValue = bloodSugarValue,
      patientUuid = patientUuid,
      facilityUuid = facilityUuid,
      userUuid = userUuid,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt,
      recordedAt = recordedAt
  )

  fun prescription(
      uuid: UUID = UUID.randomUUID(),
      name: String = faker.food.dish(),
      dosage: String? = "${faker.number.positive(10, 50)}mg",
      rxNormCode: String = "rx-norm-code",
      isDeleted: Boolean = false,
      isProtocolDrug: Boolean = false,
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.randomUUID(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      timestamps: Timestamps = Timestamps(createdAt, updatedAt, deletedAt),
      frequency: MedicineFrequency? = null,
      durationInDays: Int? = null,
      teleconsultationId: UUID? = null
  ): PrescribedDrug {
    return PrescribedDrug(
        uuid = uuid,
        name = name,
        dosage = dosage,
        rxNormCode = rxNormCode,
        isDeleted = isDeleted,
        isProtocolDrug = isProtocolDrug,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        syncStatus = syncStatus,
        timestamps = timestamps,
        frequency = frequency,
        durationInDays = durationInDays,
        teleconsultationId = teleconsultationId
    )
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
      frequency: MedicineFrequency? = null,
      durationInDays: Int? = null,
      teleconsultationId: UUID? = null
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
        frequency = frequency,
        durationInDays = durationInDays,
        teleconsultationId = teleconsultationId
    )
  }

  fun appointment(
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe"),
      scheduledDate: LocalDate = LocalDate.now(UTC).plusDays(30),
      status: Appointment.Status = Appointment.Status.random(),
      cancelReason: AppointmentCancelReason? = AppointmentCancelReason.random(),
      remindOn: LocalDate? = null,
      agreedToVisit: Boolean? = null,
      appointmentType: Appointment.AppointmentType = Appointment.AppointmentType.random(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      creationFacilityUuid: UUID? = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe")
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
        creationFacilityUuid = creationFacilityUuid
    )
  }

  fun appointmentPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      date: LocalDate = LocalDate.now(UTC).plusDays(30),
      facilityUuid: UUID = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe"),
      creationFacilityUuid: UUID? = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe"),
      status: Appointment.Status = Appointment.Status.random(),
      cancelReason: AppointmentCancelReason? = AppointmentCancelReason.random(),
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
        creationFacilityUuid = creationFacilityUuid,
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
      isOnHypertensionTreatment: Answer = randomMedicalHistoryAnswer(),
      isOnDiabetesTreatment: Answer = randomMedicalHistoryAnswer(),
      hasDiabetes: Answer = randomMedicalHistoryAnswer(),
      isSmoking: Answer = randomMedicalHistoryAnswer(),
      cholesterol: Float? = 400f,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): MedicalHistory {
    return MedicalHistory(
        uuid = uuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = diagnosedWithHypertension,
        isOnHypertensionTreatment = isOnHypertensionTreatment,
        isOnDiabetesTreatment = isOnDiabetesTreatment,
        hasHadHeartAttack = hasHadHeartAttack,
        hasHadStroke = hasHadStroke,
        hasHadKidneyDisease = hasHadKidneyDisease,
        diagnosedWithDiabetes = hasDiabetes,
        isSmoking = isSmoking,
        cholesterol = cholesterol,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
  }

  fun medicalHistoryPayload(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      hasHypertension: Answer = randomMedicalHistoryAnswer(),
      diagnosedWithHypertension: Answer = hasHypertension,
      hasHadHeartAttack: Answer = randomMedicalHistoryAnswer(),
      hasHadStroke: Answer = randomMedicalHistoryAnswer(),
      hasHadKidneyDisease: Answer = randomMedicalHistoryAnswer(),
      isOnTreatmentForHypertension: Answer = randomMedicalHistoryAnswer(),
      isOnDiabetesTreatment: Answer = randomMedicalHistoryAnswer(),
      hasDiabetes: Answer = randomMedicalHistoryAnswer(),
      isSmoking: Answer = randomMedicalHistoryAnswer(),
      cholesterol: Float? = 400f,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): MedicalHistoryPayload {
    return MedicalHistoryPayload(
        uuid = uuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = diagnosedWithHypertension,
        isOnTreatmentForHypertension = isOnTreatmentForHypertension,
        isOnDiabetesTreatment = isOnDiabetesTreatment,
        hasHadHeartAttack = hasHadHeartAttack,
        hasHadStroke = hasHadStroke,
        hasHadKidneyDisease = hasHadKidneyDisease,
        hasDiabetes = hasDiabetes,
        hasHypertension = diagnosedWithHypertension,
        isSmoking = isSmoking,
        cholesterol = cholesterol,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun ongoingPatientEntry(
      fullName: String = faker.name.name(),
      dateOfBirth: String? = null,
      age: String? = faker.number.between(MIN_ALLOWED_PATIENT_AGE, MAX_ALLOWED_PATIENT_AGE).toString(),
      gender: Gender = randomGender(),
      colony: String = faker.address.streetName(),
      district: String = faker.address.city(),
      state: String = faker.address.state(),
      streetAddress: String? = faker.address.streetAddress(),
      zone: String? = "Zone-" + faker.address.city(),
      phone: String? = faker.number.number(10),
      identifier: Identifier? = null,
      bangladeshNationalId: Identifier? = null
  ): OngoingNewPatientEntry {
    val ongoingPersonalDetails = OngoingNewPatientEntry.PersonalDetails(fullName, dateOfBirth, age, gender)
    val ongoingAddress = OngoingNewPatientEntry.Address(colony, district, state, streetAddress, zone)
    val ongoingPhoneNumber = phone?.let {
      OngoingNewPatientEntry.PhoneNumber(phone, PatientPhoneNumberType.Mobile, active = true)
    }

    return OngoingNewPatientEntry(
        personalDetails = ongoingPersonalDetails,
        address = ongoingAddress,
        phoneNumber = ongoingPhoneNumber,
        identifier = identifier,
        alternateId = bangladeshNationalId
    )
  }

  fun bloodPressureMeasurement(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe"),
      userUuid: UUID = UUID.fromString("4e3442df-ffa4-4a66-9d5f-672d3135c460"),
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
        reading = BloodPressureReading(systolic, diastolic),
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
      type: Identifier.IdentifierType = randomIdentifierType()
  ): Identifier {
    return Identifier(
        value = value,
        type = type
    )
  }

  private fun randomIdentifierType(): Identifier.IdentifierType {
    return Identifier.IdentifierType.TypeAdapter.knownMappings.keys.shuffled().first()
  }

  fun country(
      isoCountryCode: String = "IN",
      displayName: String = "India",
      isdCode: String = "91",
      deploymentName: String = "IHCI",
      deploymentEndPoint: String = "https://simple.org",
      deployments: List<Deployment> = listOf(
          deployment(
              displayName = deploymentName,
              endPoint = deploymentEndPoint
          )
      )
  ): Country {
    return Country(
        isoCountryCode = isoCountryCode,
        displayName = displayName,
        isdCode = isdCode,
        deployments = deployments
    )
  }

  fun deployment(
      displayName: String = "IHCI",
      endPoint: String = "https://simple.org",
  ): Deployment {
    return Deployment(
        displayName = displayName,
        endPoint = URI.create(endPoint)
    )
  }

  fun state(
      displayName: String = "Andhra Pradesh",
      deployment: Deployment = deployment()
  ): State {
    return State(
        displayName = displayName,
        deployment = deployment
    )
  }

  fun bloodSugarMeasurement(
      uuid: UUID = UUID.randomUUID(),
      reading: BloodSugarReading = BloodSugarReading(faker.number.decimal(2, 1), Random),
      patientUuid: UUID = UUID.randomUUID(),
      recordedAt: Instant = Instant.now(),
      userUuid: UUID = UUID.fromString("4e3442df-ffa4-4a66-9d5f-672d3135c460"),
      facilityUuid: UUID = UUID.fromString("faec54dc-1c5d-4768-83c5-80e7f272f8fe"),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      timestamps: Timestamps = Timestamps(createdAt, updatedAt, deletedAt),
      syncStatus: SyncStatus = SyncStatus.DONE
  ): BloodSugarMeasurement {
    return BloodSugarMeasurement(
        uuid = uuid,
        reading = reading,
        patientUuid = patientUuid,
        recordedAt = recordedAt,
        userUuid = userUuid,
        facilityUuid = facilityUuid,
        timestamps = timestamps,
        syncStatus = syncStatus
    )
  }

  fun overduePatientAddress(
      streetAddress: String? = faker.address.streetName(),
      colonyOrVillage: String? = faker.address.streetAddress(),
      district: String = faker.address.city(),
      state: String = faker.address.state()
  ) = OverduePatientAddress(
      streetAddress = streetAddress,
      colonyOrVillage = colonyOrVillage,
      district = district,
      state = state
  )

  fun overdueAppointment(
      facilityUuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      phoneNumberUuid: UUID = UUID.randomUUID(),
      appointmentUuid: UUID = UUID.randomUUID(),
      name: String = "somebody",
      gender: Gender = Gender.Transgender,
      phoneNumber: PatientPhoneNumber? = patientPhoneNumber(uuid = phoneNumberUuid, patientUuid = patientUuid),
      appointment: Appointment = appointment(uuid = appointmentUuid, patientUuid = patientUuid, facilityUuid = facilityUuid),
      patientAddress: OverduePatientAddress = overduePatientAddress(),
      patientAssignedFacilityId: UUID? = null,
      patientAgeDetails: PatientAgeDetails = PatientAgeDetails(
          ageValue = null,
          ageUpdatedAt = null,
          dateOfBirth = LocalDate.now(UTC).minusYears(30)
      ),
      callResult: CallResult? = null,
      eligibleForReassignment: PatientAnswer = PatientAnswer.Unanswered,
  ): OverdueAppointment {
    return OverdueAppointment(
        fullName = name,
        gender = gender,
        ageDetails = patientAgeDetails,
        phoneNumber = phoneNumber,
        patientAddress = patientAddress,
        appointment = appointment,
        callResult = callResult,
        patientAssignedFacilityUuid = patientAssignedFacilityId,
        eligibleForReassignment = eligibleForReassignment,
    )
  }

  fun loggedInUserPayload(
      uuid: UUID = UUID.randomUUID(),
      name: String = "a name",
      phone: String = "a phone",
      pinDigest: String = "a hash",
      registrationFacilityUuid: UUID = UUID.randomUUID(),
      status: UserStatus = UserStatus.WaitingForApproval,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      teleconsultPhoneNumber: String = "1111111111",
      capabilities: User.Capabilities? = null
  ): LoggedInUserPayload {
    return LoggedInUserPayload(
        uuid = uuid,
        fullName = name,
        phoneNumber = phone,
        pinDigest = pinDigest,
        registrationFacilityId = registrationFacilityUuid,
        createdAt = createdAt,
        status = status,
        updatedAt = updatedAt,
        teleconsultPhoneNumber = teleconsultPhoneNumber,
        capabilities = capabilities
    )
  }

  fun patientSearchResult(
      uuid: UUID = UUID.randomUUID(),
      fullName: String = "Ashok Kumar",
      phoneNumber: String = "3.14159",
      gender: Gender = Gender.Male,
      status: PatientStatus = PatientStatus.Active,
      address: PatientAddress = patientAddress(),
      assignedFacilityId: UUID? = null,
      identifier: Identifier = Identifier(
          value = UUID.randomUUID().toString(),
          type = BpPassport
      ),
      identifierSearchHelp: String? = null,
      assignedFacilityName: String? = null,
      patientAgeDetails: PatientAgeDetails = PatientAgeDetails(
          ageValue = 45,
          ageUpdatedAt = Instant.now(),
          dateOfBirth = null
      ),
      eligibleForReassignment: PatientAnswer = PatientAnswer.Unanswered,
  ): PatientSearchResult {
    return PatientSearchResult(
        uuid = uuid,
        fullName = fullName,
        gender = gender,
        ageDetails = patientAgeDetails,
        status = status,
        assignedFacilityId = assignedFacilityId,
        assignedFacilityName = assignedFacilityName,
        address = address,
        phoneNumber = phoneNumber,
        identifier = identifier,
        identifierSearchHelp = identifierSearchHelp,
        eligibleForReassignment = eligibleForReassignment,
    )
  }

  fun recentPatient(
      uuid: UUID = UUID.randomUUID(),
      fullName: String = "fullName",
      gender: Gender = randomGender(),
      patientRecordedAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      patientAgeDetails: PatientAgeDetails = PatientAgeDetails(
          ageValue = null,
          ageUpdatedAt = null,
          dateOfBirth = null
      ),
      eligibleForReassignment: PatientAnswer = PatientAnswer.Unanswered,
  ) = RecentPatient(
      uuid = uuid,
      fullName = fullName,
      gender = gender,
      ageDetails = patientAgeDetails,
      patientRecordedAt = patientRecordedAt,
      updatedAt = updatedAt,
      eligibleForReassignment = eligibleForReassignment,
  )

  fun ongoingLoginEntry(
      uuid: UUID = UUID.fromString("e4d91a34-c475-4038-a066-a866f9ecafec"),
      phoneNumber: String? = "1111111111",
      pin: String? = "1234",
      fullName: String? = "Anish Acharya",
      pinDigest: String? = "digest",
      registrationFacilityUuid: UUID? = UUID.fromString("d91fb2ba-9c87-4de0-b425-eea44457c746"),
      status: UserStatus? = UserStatus.ApprovedForSyncing,
      createdAt: Instant? = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt: Instant? = Instant.parse("2018-01-01T00:00:00Z"),
      teleconsultPhoneNumber: String = "+911111111111",
      capabilities: User.Capabilities? = null
  ) = OngoingLoginEntry(
      uuid = uuid,
      phoneNumber = phoneNumber,
      pin = pin,
      fullName = fullName,
      pinDigest = pinDigest,
      registrationFacilityUuid = registrationFacilityUuid,
      status = status,
      createdAt = createdAt,
      updatedAt = updatedAt,
      capabilities = capabilities,
      teleconsultPhoneNumber = teleconsultPhoneNumber
  )

  fun medicalOfficer(
      id: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      phoneNumber: String = faker.phoneNumber.cellPhone()
  ): MedicalOfficer {
    return MedicalOfficer(
        medicalOfficerId = id,
        fullName = fullName,
        phoneNumber = phoneNumber
    )
  }

  fun teleconsultationFacilityInfo(
      id: UUID = UUID.randomUUID(),
      facilityId: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      deletedAt: Instant? = null,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): TeleconsultationFacilityInfo {
    return TeleconsultationFacilityInfo(
        teleconsultationFacilityId = id,
        facilityId = facilityId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = syncStatus
    )
  }

  fun teleconsultationFacilityWithMedicalOfficers(
      teleconsultationFacilityInfo: TeleconsultationFacilityInfo = teleconsultationFacilityInfo(),
      medicalOfficers: List<MedicalOfficer> = emptyList()
  ): TeleconsultationFacilityWithMedicalOfficers {
    return TeleconsultationFacilityWithMedicalOfficers(
        teleconsultationFacilityInfo = teleconsultationFacilityInfo,
        medicalOfficers = medicalOfficers
    )
  }

  fun teleconsultationFacilityInfoMedicalOfficersCrossRef(
      teleconsultationFacilityUuid: UUID = UUID.randomUUID(),
      medicalOfficerUuid: UUID = UUID.randomUUID()
  ): TeleconsultationFacilityMedicalOfficersCrossRef {
    return TeleconsultationFacilityMedicalOfficersCrossRef(
        teleconsultationFacilityId = teleconsultationFacilityUuid,
        medicalOfficerId = medicalOfficerUuid
    )
  }

  fun medicalOfficerPayload(
      id: UUID = UUID.randomUUID(),
      fullName: String = faker.name.name(),
      phoneNumber: String = faker.phoneNumber.cellPhone()
  ): MedicalOfficerPayload {
    return MedicalOfficerPayload(
        id = id,
        fullName = fullName,
        phoneNumber = phoneNumber
    )
  }

  fun teleconsultationFacilityInfoPayload(
      id: UUID = UUID.randomUUID(),
      facilityId: UUID = UUID.randomUUID(),
      medicalOfficersPayload: List<MedicalOfficerPayload> = emptyList(),
      createdAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      deletedAt: Instant? = null,
  ): TeleconsultationFacilityInfoPayload {
    return TeleconsultationFacilityInfoPayload(
        id = id,
        facilityId = facilityId,
        medicalOfficers = medicalOfficersPayload,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
  }

  fun teleconsultRecord(
      id: UUID = UUID.randomUUID(),
      patientId: UUID = UUID.randomUUID(),
      medicalOfficerId: UUID = UUID.randomUUID(),
      teleconsultRequestInfo: TeleconsultRequestInfo? = null,
      teleconsultRecordInfo: TeleconsultRecordInfo? = null,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      timestamps: Timestamps = Timestamps(createdAt, updatedAt, deletedAt),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): TeleconsultRecord {
    return TeleconsultRecord(
        id = id,
        patientId = patientId,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfo,
        teleconsultRecordInfo = teleconsultRecordInfo,
        timestamp = timestamps,
        syncStatus = syncStatus
    )
  }

  fun teleconsultRecordInfo(
      recordedAt: Instant = Instant.parse("2020-09-03T00:00:00Z"),
      teleconsultationType: TeleconsultationType = randomTeleconsultationType(),
      patientTookMedicines: TeleconsultRecordAnswer = randomTeleconsultRecordAnswer(),
      patientConsented: TeleconsultRecordAnswer = randomTeleconsultRecordAnswer(),
      medicalOfficerNumber: String? = "22222222"
  ): TeleconsultRecordInfo {
    return TeleconsultRecordInfo(
        recordedAt = recordedAt,
        teleconsultationType = teleconsultationType,
        patientTookMedicines = patientTookMedicines,
        patientConsented = patientConsented,
        medicalOfficerNumber = medicalOfficerNumber
    )
  }

  fun teleconsultRequestInfo(
      requesterId: UUID = UUID.randomUUID(),
      facilityId: UUID = UUID.randomUUID(),
      requestedAt: Instant = Instant.parse("2020-09-02T00:00:00Z"),
      requesterCompletionStatus: TeleconsultStatus? = null
  ): TeleconsultRequestInfo {
    return TeleconsultRequestInfo(
        requesterId = requesterId,
        facilityId = facilityId,
        requestedAt = requestedAt,
        requesterCompletionStatus = requesterCompletionStatus
    )
  }

  fun indiaNHIDInfoPayload(
      healthIdNumber: String = "1234123456785678",
      healthIdUserName: String = "Mohit",
      fullName: String = "Mohit Ahuja",
      indiaNHIDGender: IndiaNHIDGender = IndiaNHIDGender.MALE,
      state: String = "Maharashtra",
      district: String = "Thane",
      dateOfBirth: LocalDate = LocalDate.parse("1997-12-12"),
      address: String = "Obvious HQ"
  ) = IndiaNHIDInfoPayload(
      healthIdNumber = healthIdNumber,
      healthIdUserName = healthIdUserName,
      fullName = fullName,
      indiaNHIDGender = indiaNHIDGender,
      state = state,
      district = district,
      dateOfBirth = dateOfBirth,
      address = address
  )

  fun drug(
      id: UUID = UUID.randomUUID(),
      name: String = "Amolodipine",
      category: DrugCategory? = CCB,
      frequency: DrugFrequency? = OD,
      composition: String? = null,
      dosage: String? = "10 mg",
      rxNormCode: String? = "329526",
      protocol: DrugAnswer = Yes,
      common: DrugAnswer = Yes,
      createdAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      deletedAt: Instant? = null
  ) = Drug(
      id = id,
      name = name,
      category = category,
      frequency = frequency,
      composition = composition,
      dosage = dosage,
      rxNormCode = rxNormCode,
      protocol = protocol,
      common = common,
      timestamps = Timestamps(
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      )
  )

  fun patientPrefillInfo(
      fullName: String = "Christi Joe",
      gender: Gender = Gender.Female,
      dateOfBirth: LocalDate = LocalDate.parse("2021-05-12"),
      address: String = "Obvious HQ"
  ) = PatientPrefillInfo(
      fullName = fullName,
      gender = gender,
      dateOfBirth = dateOfBirth,
      address = address
  )

  fun completeMedicalRecord(
      patient: PatientProfile = patientProfile(patientRegisteredFacilityId = UUID.fromString("a3ae4eac-7cad-4b3f-bf32-f0002a3e9eef")),
      medicalHistory: MedicalHistory = medicalHistory(),
      appointments: List<Appointment> = listOf(appointment(facilityUuid = UUID.fromString("a3ae4eac-7cad-4b3f-bf32-f0002a3e9eef"))),
      bloodPressures: List<BloodPressureMeasurement> = listOf(bloodPressureMeasurement(facilityUuid = UUID.fromString("a3ae4eac-7cad-4b3f-bf32-f0002a3e9eef"))),
      bloodSugars: List<BloodSugarMeasurement> = listOf(bloodSugarMeasurement(facilityUuid = UUID.fromString("a3ae4eac-7cad-4b3f-bf32-f0002a3e9eef"))),
      prescribedDrugs: List<PrescribedDrug> = listOf(prescription(facilityUuid = UUID.fromString("a3ae4eac-7cad-4b3f-bf32-f0002a3e9eef")))
  ) = CompleteMedicalRecord(
      patient = patient,
      medicalHistory = medicalHistory,
      appointments = appointments,
      bloodPressures = bloodPressures,
      bloodSugars = bloodSugars,
      prescribedDrugs = prescribedDrugs
  )


  fun contactPatientProfile(
      patientUuid: UUID = UUID.randomUUID(),
      patientAddressUuid: UUID = UUID.randomUUID(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      generatePhoneNumber: Boolean = faker.bool.bool(),
      generateBusinessId: Boolean = faker.bool.bool(),
      patientStatus: PatientStatus = PatientStatus.Active,
      patientDeletedAt: Instant? = null,
      patientName: String = faker.name.name(),
      patientPhoneNumber: String? = if (generatePhoneNumber) faker.phoneNumber.phoneNumber() else null,
      businessId: BusinessId? = if (generateBusinessId) businessId(patientUuid = patientUuid) else null,
      generateDateOfBirth: Boolean = faker.bool.bool(),
      gender: Gender = randomGender(),
      patientDeletedReason: DeletedReason? = null,
      patientCreatedAt: Instant = Instant.parse("2021-07-01T00:00:00Z"),
      patientUpdatedAt: Instant = Instant.parse("2021-07-05T00:00:00Z"),
      patientRecordedAt: Instant = Instant.parse("2021-07-01T00:00:00Z"),
      patientRegisteredFacilityId: UUID = UUID.randomUUID(),
      patientAssignedFacilityId: UUID? = null,
      retainUntil: Instant? = null,
      patientAgeDetails: PatientAgeDetails = PatientAgeDetails(
          ageValue = if (!generateDateOfBirth) Math.random().times(100).toInt() else null,
          ageUpdatedAt = if (!generateDateOfBirth) Instant.now() else null,
          dateOfBirth = if (generateDateOfBirth) LocalDate.parse("2018-01-01") else null
      )
  ): ContactPatientProfile {
    val phoneNumbers = if (!patientPhoneNumber.isNullOrBlank()) {
      listOf(patientPhoneNumber(patientUuid = patientUuid, number = patientPhoneNumber, phoneType = PatientPhoneNumberType.Mobile))
    } else {
      emptyList()
    }

    return ContactPatientProfile(
        patient = patient(
            uuid = patientUuid,
            addressUuid = patientAddressUuid,
            fullName = patientName,
            gender = gender,
            status = patientStatus,
            createdAt = patientCreatedAt,
            updatedAt = patientUpdatedAt,
            deletedAt = patientDeletedAt,
            recordedAt = patientRecordedAt,
            syncStatus = syncStatus,
            deletedReason = patientDeletedReason,
            registeredFacilityId = patientRegisteredFacilityId,
            assignedFacilityId = patientAssignedFacilityId,
            retainUntil = retainUntil,
            patientAgeDetails = patientAgeDetails
        ),
        address = patientAddress(uuid = patientAddressUuid),
        phoneNumbers = phoneNumbers,
        registeredFacility = facility(uuid = patientRegisteredFacilityId),
        medicalHistory = medicalHistory(patientUuid = patientUuid),
        bloodSugarMeasurement = bloodSugarMeasurement(patientUuid = patientUuid),
        bloodPressureMeasurement = bloodPressureMeasurement(patientUuid = patientUuid))
  }

  fun callResult(
      id: UUID = UUID.fromString("512a1c6e-3b11-4dd7-bda9-e0f99a092ace"),
      userId: UUID = UUID.fromString("80115bd3-d8be-4426-9e71-c0550ba2a495"),
      appointmentId: UUID = UUID.fromString("a2a467c9-1695-4e72-833d-37ce0cb3534a"),
      removeReason: AppointmentCancelReason? = null,
      outcome: Outcome = Outcome.AgreedToVisit,
      createdAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt: Instant = Instant.parse("2018-01-01T00:00:00Z"),
      deletedAt: Instant? = null,
      syncStatus: SyncStatus = SyncStatus.DONE,
      patientId: UUID = UUID.fromString("53c27f08-8f63-4c41-8495-530f9e482e23"),
      facilityUuid: UUID = UUID.fromString("38e04f2b-95ff-4a76-a24d-0b3b73d64eca")
  ) = CallResult(
      id = id,
      userId = userId,
      appointmentId = appointmentId,
      removeReason = removeReason,
      outcome = outcome,
      timestamps = Timestamps(createdAt, updatedAt, deletedAt),
      syncStatus = syncStatus,
      patientId = patientId,
      facilityId = facilityUuid
  )

  fun questionnaire(
      uuid: UUID = UUID.fromString("f9a42c9f-01fe-40c5-b625-64b3e9868d5e"),
      questionnaireType: QuestionnaireType = MonthlyScreeningReports,
      layout: BaseComponentData = getTestQuestionnaireLayout()
  ) = Questionnaire(
      uuid = uuid,
      questionnaire_type = questionnaireType,
      layout = layout,
      deletedAt = null
  )

  private fun getTestQuestionnaireLayout() = ViewGroupComponentData(
      id = UUID.fromString("ff78b2d4-3e95-457c-bbb0-57cb8c2b2715").toString(),
      type = "group",
      children = listOf(
          SubHeaderComponentData(
              type = "display",
              text = "Monthly OPD visits for adults >30 years old",
              id = UUID.fromString("269045f7-c504-4ac4-9e63-864baf298bc8").toString()
          ),
          InputViewGroupComponentData(
              id = UUID.fromString("6901b3fd-2d06-4366-b7fb-ccb0fbc6a539").toString(),
              type = "group",
              children = listOf(
                  InputFieldComponentData(
                      id = "outpatient_department_visits",
                      linkId = "monthly_screening_reports.outpatient_department_visits",
                      text = "Outpatient department visits",
                      type = IntegerType,
                      validations = InputFieldValidations(min = 0, max = 1000000),
                      viewType = null,
                      viewFormat = null
                  )
              )
          ),
          HeaderComponentData(
              type = "display",
              text = "HTN & DM SCREENING",
              id = UUID.fromString("f8527375-eb6a-4e9f-a261-ae8afecb1ac6").toString()
          ),
          SubHeaderComponentData(
              type = "display",
              text = "Total BP Checks done",
              id = UUID.fromString("38cf0223-1b89-4676-a71b-68a20c30a543").toString()
          ),
          InputViewGroupComponentData(
              type = "display",
              id = UUID.fromString("6901b3fd-2d06-4366-b7fb-ccb0fbc6a539").toString(),
              children = listOf(
                  InputFieldComponentData(
                      id = "blood_pressure_checks_male",
                      linkId = "monthly_screening_reports.blood_pressure_checks_male",
                      text = "Male",
                      type = IntegerType,
                      validations = InputFieldValidations(min = 0, max = 1000000),
                      viewType = null,
                      viewFormat = null
                  ),
                  InputFieldComponentData(
                      id = "blood_pressure_checks_female",
                      linkId = "monthly_screening_reports.blood_pressure_checks_female",
                      text = "Female",
                      type = IntegerType,
                      validations = InputFieldValidations(min = 0, max = 1000000),
                      viewType = null,
                      viewFormat = null
                  )
              )
          ),
          SeparatorComponentData(
              id = UUID.fromString("9d38cb13-4f6b-4bcc-af43-3fd80f2edc18").toString(),
              type = "display"
          ),
          LineSeparatorComponentData(
              id = UUID.fromString("154b6890-52c3-410d-8dd0-4eb11ec1a680").toString(),
              type = "display"
          ),
          ParagraphComponentData(
              id = UUID.fromString("6b9e32a6-700b-4baf-9b9a-7a42c7e43e30").toString(),
              type = "display",
              text = "Enter the supplies left in stock at the end of every month"
          ),
          UnorderedListViewGroupComponentData(
              id = UUID.fromString("07aa1114-d633-48af-8bd9-290da5c83f77").toString(),
              type = "group",
              children = listOf(
                  UnorderedListItemComponentData(
                      id = UUID.fromString("194d1ccd-73ac-455f-a616-edffc74c8705").toString(),
                      type = "display",
                      icon = "check",
                      iconColor = "#00FF00",
                      text = "Leave blank if you don't know an amount"
                  ),
                  UnorderedListItemComponentData(
                      id = UUID.fromString("10d9b118-e8f3-4b71-bf89-9cb20af50616").toString(),
                      type = "display",
                      icon = "close",
                      iconColor = "#FF0000",
                      text = "Enter 0 if stock is out"
                  )
              )
          ),
          RadioViewGroupComponentData(
              id = UUID.fromString("3399a261-a8f0-45ac-adee-6e024531350d").toString(),
              type = "group",
              linkId = "monthly_supplies_report.wifi_available",
              children = listOf(
                  RadioButtonComponentData(
                      id = UUID.fromString("f616c4e0-d261-4fcc-ba65-7d6b60b10241").toString(),
                      type = "radio",
                      text = "Yes"
                  ),
                  RadioButtonComponentData(
                      id = UUID.fromString("918fa950-5928-4bac-b586-8da196169063").toString(),
                      type = "radio",
                      text = "No"
                  )
              )
          ),
          InputViewGroupComponentData(
              type = "display",
              id = UUID.fromString("09836226-a57b-4da6-a0db-7015a4697248").toString(),
              children = listOf(
                  InputFieldComponentData(
                      id = UUID.fromString("9f8b8cf4-006f-4c72-9a99-ab3e2f5c881e").toString(),
                      linkId = "monthly_supplies_report.comments",
                      text = "",
                      type = StringType,
                      validations = InputFieldValidations(min = 0, max = 1000),
                      viewType = null,
                      viewFormat = null
                  ),
              )
          ),
      )
  )

  fun questionnaireResponse(
      uuid: UUID = UUID.fromString("b780d18c-93df-4d0d-a18c-02f72ca757be"),
      questionnaireId: UUID = UUID.fromString("84801306-d28e-4755-b027-8e6f80e67daa"),
      questionnaireType: QuestionnaireType = MonthlyScreeningReports,
      facilityId: UUID = UUID.fromString("7ac2d657-6868-441c-9c0c-5c4a5dba87d7"),
      lastUpdatedByUserId: UUID = UUID.fromString("773810ea-850f-40f1-8ec2-259adc3549a3"),
      content: Map<String, Any?> = getQuestionnaireResponseContent(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
  ): QuestionnaireResponse {
    return QuestionnaireResponse(
        uuid = uuid,
        questionnaireId = questionnaireId,
        questionnaireType = questionnaireType,
        facilityId = facilityId,
        lastUpdatedByUserId = lastUpdatedByUserId,
        content = content,
        timestamps = Timestamps(createdAt, updatedAt, deletedAt),
        syncStatus = syncStatus
    )
  }

  fun getQuestionnaireResponseContent(): Map<String, Any?> {
    return mapOf(
        "monthly_screening_reports.outpatient_department_visits" to 5000.0,
        "monthly_screening_reports.blood_pressure_checks_male" to 2200.0,
        "monthly_screening_reports.blood_pressure_checks_female" to 1800.0,
        "monthly_screening_reports.gender" to "Male",
        "monthly_screening_reports.is_smoking" to true,
    )
  }

  fun patientAttribute(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      userUuid: UUID = UUID.randomUUID(),
      reading: BMIReading,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): PatientAttribute {
    return PatientAttribute(
        uuid = uuid,
        patientUuid = patientUuid,
        userUuid = userUuid,
        bmiReading = reading,
        timestamps = Timestamps(
            createdAt, updatedAt, deletedAt
        ),
        syncStatus = syncStatus
    )
  }

  fun cvdRisk(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      riskScore: CVDRiskRange,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): CVDRisk {
    return CVDRisk(
        uuid = uuid,
        patientUuid = patientUuid,
        riskScore = riskScore,
        timestamps = Timestamps(
            createdAt, updatedAt, deletedAt
        ),
        syncStatus = syncStatus
    )
  }

  fun nonLabBasedCVDRiskCalculationSheet(): NonLabBasedCVDRiskCalculationSheet {
    val smokingDataWomen = AgeData(
        age40to44 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 11),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 9),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 5),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 6),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 6)
        ),
        age45to49 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 11),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 9),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 5),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 6),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 6)
        ),
        age50to54 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 11),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 9),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 5),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 6),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 6)
        ),
        age55to59 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 11),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 9),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 5),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 6),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 6)
        ),
        age60to64 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 21),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 18),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 12),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 13),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 14)
        ),
        age65to69 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 11),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 9),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 5),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 6),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 6)
        ),
        age70to74 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 11),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 9),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 5),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 6),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 6)
        )
    )

    val nonSmokingDataWomen = AgeData(
        age40to44 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 5),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 4),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 2)
        ),
        age45to49 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 5),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 4),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 2)
        ),
        age50to54 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 5),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 4),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 2)
        ),
        age55to59 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 5),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 4),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 2)
        ),
        age60to64 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 13),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 11),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 7),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 8),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 8)
        ),
        age65to69 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 5),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 4),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 2)
        ),
        age70to74 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 5),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 4),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "25 - 29", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 2),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 2)
        )
    )

    val womenGenderData = Women(
        smoking = smokingDataWomen,
        nonSmoking = nonSmokingDataWomen
    )

    val smokingDataMen = smokingDataWomen.copy(
        age40to44 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 10),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 9),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 6),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "30 - 35", risk = 7),
            NonLabBasedRiskEntry(systolic = "120 - 139", bmi = "> 35", risk = 8)
        )
    )

    val nonSmokingDataMen = nonSmokingDataWomen.copy(
        age40to44 = listOf(
            NonLabBasedRiskEntry(systolic = ">= 180", bmi = "< 20", risk = 5),
            NonLabBasedRiskEntry(systolic = "160 - 179", bmi = "20 - 24", risk = 5),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "25 - 29", risk = 3),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "30 - 35", risk = 3),
            NonLabBasedRiskEntry(systolic = "140 - 159", bmi = "> 35", risk = 4)
        )
    )

    val menGenderData = Men(
        smoking = smokingDataMen,
        nonSmoking = nonSmokingDataMen
    )

    return NonLabBasedCVDRiskCalculationSheet(
        women = womenGenderData,
        men = menGenderData
    )
  }

  fun labBasedCVDRiskCalculationSheet(): LabBasedCVDRiskCalculationSheet {
    val smokingDataWomen = AgeData(
        age40to44 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 11),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "< 4", risk = 7),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "4 - 4.9", risk = 8),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 10),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "6 - 6.9", risk = 11),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = ">= 7", risk = 13),
        ),
        age45to49 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 11),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 9),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 5),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 6),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 6)
        ),
        age50to54 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 11),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 9),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 5),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 6),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 6)
        ),
        age55to59 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 11),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 9),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 5),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 6),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 6)
        ),
        age60to64 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 11),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 9),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 5),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 6),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 6)
        ),
        age65to69 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 11),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 9),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 5),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 6),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 6)
        ),
        age70to74 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 11),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 9),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 5),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 6),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 6)
        )
    )

    val nonSmokingDataWomen = AgeData(
        age40to44 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 5),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "5 - 5.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 2)
        ),
        age45to49 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 5),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "5 - 5.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 2)
        ),
        age50to54 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 5),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "5 - 5.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 2)
        ),
        age55to59 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 5),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "5 - 5.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 2)
        ),
        age60to64 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 5),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "5 - 5.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 2)
        ),
        age65to69 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 5),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "5 - 5.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 2)
        ),
        age70to74 = listOf(
            LabBasedRiskEntry(systolic = ">= 180", cholesterol = "< 4", risk = 5),
            LabBasedRiskEntry(systolic = "160 - 179", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "5 - 5.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = "6 - 6.9", risk = 2),
            LabBasedRiskEntry(systolic = "120 - 139", cholesterol = ">= 7", risk = 2)
        )
    )

    val womenGenderData = Women(
        smoking = smokingDataWomen,
        nonSmoking = nonSmokingDataWomen
    )

    val smokingDataMen = smokingDataWomen.copy(
        age40to44 = listOf(
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "< 4", risk = 7),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "4 - 4.9", risk = 8),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 9),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "6 - 6.9", risk = 11),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = ">= 7", risk = 14),
        )
    )

    val nonSmokingDataMen = nonSmokingDataWomen.copy(
        age40to44 = listOf(
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "< 4", risk = 4),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "4 - 4.9", risk = 4),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "5 - 5.9", risk = 4),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = "6 - 6.9", risk = 5),
            LabBasedRiskEntry(systolic = "140 - 159", cholesterol = ">= 7", risk = 6),
        )
    )

    val menGenderData = Men(
        smoking = smokingDataMen,
        nonSmoking = nonSmokingDataMen
    )

    return LabBasedCVDRiskCalculationSheet(
        diabetes = LabBasedCVDRiskCalculationSheet.DiabetesRisk(
            women = womenGenderData,
            men = menGenderData
        ),
        noDiabetes = LabBasedCVDRiskCalculationSheet.DiabetesRisk(
            women = womenGenderData,
            men = menGenderData
        )
    )
  }
}
