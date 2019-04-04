package org.simple.clinic.patient

import com.nhaarman.mockito_kotlin.mock
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.location.Coordinates
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.NO
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.YES
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.patient.recent.RecentPatient
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZoneOffset.UTC
import java.util.Random
import java.util.UUID
import kotlin.reflect.KClass

private fun <T : Enum<T>> randomOfEnum(enumClass: KClass<T>): T {
  return enumClass.java.enumConstants.asList().shuffled().first()
}

object PatientMocker {

  private val random = Random()

  fun patient(
      uuid: UUID = UUID.randomUUID(),
      addressUuid: UUID = UUID.randomUUID(),
      fullName: String = "name",
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      status: PatientStatus = PatientStatus.ACTIVE,
      gender: Gender = randomOfEnum(Gender::class),
      dateOfBirth: LocalDate? = LocalDate.now(ZoneOffset.UTC),
      age: Age? = null,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = addressUuid,
        fullName = fullName,
        searchableName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        age = age,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = syncStatus)
  }

  fun address(
      uuid: UUID = UUID.randomUUID(),
      colonyOrVillage: String? = "colony/village",
      district: String = "district",
      state: String = "state",
      country: String = "India",
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null
  ): PatientAddress {
    return PatientAddress(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        district = district,
        state = state,
        country = country,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun bp(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      systolic: Int = random.nextInt(100) + 100,
      diastolic: Int = random.nextInt(100),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      userUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.randomUUID(),
      deletedAt: Instant? = null
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
        deletedAt = deletedAt)
  }

  fun facility(
      uuid: UUID = mock(),
      name: String = "some facility",
      streetAddress: String? = "some street",
      district: String = "district",
      state: String = "state",
      facilityType: String? = null,
      villageOrColony: String? = null,
      country: String = "India",
      pinCode: String? = null,
      protocolUuid: UUID? = UUID.randomUUID(),
      groupUuid: UUID? = UUID.randomUUID(),
      location: Coordinates? = Coordinates(latitude = 1.908537, longitude = 73.537524),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class)
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
      uuid: UUID = mock(),
      name: String = "some facility",
      streetAddress: String? = "some street",
      district: String = "district",
      state: String = "state",
      facilityType: String? = null,
      villageOrColony: String? = null,
      country: String = "India",
      pinCode: String? = null,
      protocolUuid: UUID = UUID.randomUUID(),
      groupUuid: UUID = UUID.randomUUID(),
      locationLatitude: Double? = 1.908537,
      locationLongitude: Double? = 73.537524,
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

  fun prescription(
      uuid: UUID = mock(),
      name: String = "drug name",
      dosage: String? = "dosage",
      isProtocolDrug: Boolean = false,
      syncStatus: SyncStatus = randomOfEnum(SyncStatus::class),
      patientUuid: UUID = UUID.randomUUID(),
      facilityUuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant? = null,
      isDeleted: Boolean = false
  ): PrescribedDrug {
    return PrescribedDrug(
        uuid = uuid,
        name = name,
        dosage = dosage,
        rxNormCode = "rx-norm-code",
        isDeleted = isDeleted,
        isProtocolDrug = isProtocolDrug,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  fun protocolDrug(
      uuid: UUID = UUID.randomUUID(),
      name: String = "drug name",
      dosage: String = "5mg",
      protocolUuid: UUID = UUID.randomUUID(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      deletedAt: Instant = Instant.now(),
      order: Int = 0
  ): ProtocolDrug {
    return ProtocolDrug(
        uuid = uuid,
        name = name,
        rxNormCode = "rxnormcode-1",
        dosage = dosage,
        protocolUuid = protocolUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        order = order)
  }

  fun appointment(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      scheduledDate: LocalDate = LocalDate.now(UTC),
      facilityUuid: UUID = UUID.randomUUID(),
      status: Appointment.Status = Appointment.Status.SCHEDULED,
      cancelReason: AppointmentCancelReason? = AppointmentCancelReason.PatientNotResponding,
      syncStatus: SyncStatus = SyncStatus.PENDING,
      agreedToVisit: Boolean? = null,
      remindOn: LocalDate? = LocalDate.now(UTC).minusDays(2),
      appointmentType: Appointment.AppointmentType? = null,
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
        deletedAt = deletedAt
    )
  }

  fun overdueAppointment(
      name: String = "somebody",
      bloodPressureMeasurement: BloodPressureMeasurement = bp(),
      riskLevelIndex: Int? = null,
      riskLevel: OverdueAppointment.RiskLevel? = null
  ): OverdueAppointment {
    if ((riskLevel == null) == (riskLevelIndex == null)) {
      throw AssertionError("Both riskLevel and riskLevelIndex cannot be null or non-null")
    }

    val calculatedRiskLevel = when {
      riskLevel != null -> riskLevel.levelIndex
      riskLevelIndex != null -> riskLevelIndex
      else -> throw AssertionError("Both riskLevel and riskLevelIndex cannot be null")
    }

    return OverdueAppointment(
        fullName = name,
        gender = mock(),
        dateOfBirth = LocalDate.now(ZoneOffset.UTC).minusYears(30),
        age = null,
        phoneNumber = mock(),
        appointment = appointment(),
        bloodPressure = bloodPressureMeasurement,
        riskLevelIndex = calculatedRiskLevel
    )
  }

  fun loggedInUser(
      uuid: UUID = UUID.randomUUID(),
      name: String = "a name",
      phone: String = "a phone",
      pinDigest: String = "a hash",
      status: UserStatus = UserStatus.WAITING_FOR_APPROVAL,
      loggedInStatus: User.LoggedInStatus = User.LoggedInStatus.LOGGED_IN
  ): User {
    return User(
        uuid = uuid,
        fullName = name,
        phoneNumber = phone,
        pinDigest = pinDigest,
        createdAt = mock(),
        status = status,
        updatedAt = mock(),
        loggedInStatus = loggedInStatus
    )
  }

  fun loggedInUserPayload(
      uuid: UUID = UUID.randomUUID(),
      name: String = "a name",
      phone: String = "a phone",
      pinDigest: String = "a hash",
      facilityUuids: List<UUID> = listOf(UUID.randomUUID(), UUID.randomUUID()),
      status: UserStatus = UserStatus.WAITING_FOR_APPROVAL,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): LoggedInUserPayload {
    return LoggedInUserPayload(
        uuid = uuid,
        fullName = name,
        phoneNumber = phone,
        pinDigest = pinDigest,
        facilityUuids = facilityUuids,
        createdAt = createdAt,
        status = status,
        updatedAt = updatedAt)
  }

  fun patientSearchResult(
      uuid: UUID = UUID.randomUUID(),
      fullName: String = "Ashok Kumar",
      phoneNumber: String = "3.14159"
  ): PatientSearchResult {
    return PatientSearchResult(
        uuid = uuid,
        fullName = fullName,
        gender = mock(),
        dateOfBirth = null,
        age = mock(),
        status = PatientStatus.ACTIVE,
        createdAt = mock(),
        updatedAt = mock(),
        address = PatientMocker.address(),
        syncStatus = mock(),
        phoneNumber = phoneNumber,
        phoneType = mock(),
        phoneUuid = mock(),
        phoneActive = true,
        phoneCreatedAt = mock(),
        phoneUpdatedAt = mock(),
        lastBp = PatientSearchResult.LastBp(
            takenOn = Instant.now(),
            takenAtFacilityName = "Some Facility",
            takenAtFacilityUuid = UUID.randomUUID()))
  }

  fun medicalHistory(
      hasHadHeartAttack: Answer = YES,
      hasHadStroke: Answer = NO,
      hasHadKidneyDisease: Answer = YES,
      diagnosedWithHypertension: Answer = NO,
      isOnTreatmentForHypertension: Answer = YES,
      hasDiabetes: Answer = YES,
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      syncStatus: SyncStatus = SyncStatus.PENDING,
      deletedAt: Instant? = null
  ): MedicalHistory {
    return MedicalHistory(
        uuid = UUID.randomUUID(),
        patientUuid = UUID.randomUUID(),
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

  fun phoneNumber(
      uuid: UUID = UUID.randomUUID(),
      patientUuid: UUID = UUID.randomUUID(),
      number: String = "1111111111",
      phoneType: PatientPhoneNumberType = randomOfEnum(PatientPhoneNumberType::class),
      active: Boolean = true,
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

  fun recentPatient(
      uuid: UUID = UUID.randomUUID(),
      fullName: String = "fullName",
      gender: Gender = randomOfEnum(Gender::class),
      dateOfBirth: LocalDate? = null,
      age: Age? = null,
      lastBp: RecentPatient.LastBp? = null
  ) = RecentPatient(
      uuid = uuid,
      fullName = fullName,
      gender = gender,
      dateOfBirth = dateOfBirth,
      age = age,
      lastBp = lastBp
  )
}
