package org.simple.clinic.patient

import com.nhaarman.mockito_kotlin.mock
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
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
      syncStatus: SyncStatus = mock(),
      status: PatientStatus = PatientStatus.ACTIVE
  ): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = addressUuid,
        fullName = fullName,
        searchableName = fullName,
        gender = mock(),
        dateOfBirth = mock(),
        age = mock(),
        status = status,
        createdAt = mock(),
        updatedAt = mock(),
        syncStatus = syncStatus)
  }

  fun address(
      uuid: UUID = mock()
  ): PatientAddress {
    return PatientAddress(
        uuid = uuid,
        colonyOrVillage = "colony/village",
        district = "district",
        state = "state",
        country = "India",
        createdAt = mock(),
        updatedAt = mock())
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
      facilityUuid: UUID = UUID.randomUUID()
  ): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = uuid,
        systolic = systolic,
        diastolic = diastolic,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        userUuid = userUuid,
        facilityUuid = facilityUuid,
        patientUuid = patientUuid)
  }

  fun facility(
      uuid: UUID = mock(),
      name: String = "some facility",
      district: String = "district",
      state: String = "state"
  ): Facility {
    return Facility(
        uuid = uuid,
        name = name,
        district = district,
        state = state,
        facilityType = null,
        streetAddress = null,
        villageOrColony = null,
        country = "India",
        pinCode = null,
        createdAt = mock(),
        updatedAt = mock(),
        syncStatus = mock())
  }

  fun prescription(
      uuid: UUID = mock(),
      name: String = "drug name",
      dosage: String? = "dosage",
      isProtocolDrug: Boolean = false,
      syncStatus: SyncStatus = mock()
  ): PrescribedDrug {
    return PrescribedDrug(
        uuid = uuid,
        name = name,
        dosage = dosage,
        rxNormCode = "rx-norm-code",
        isDeleted = false,
        isProtocolDrug = isProtocolDrug,
        patientUuid = mock(),
        facilityUuid = mock(),
        syncStatus = syncStatus,
        createdAt = mock(),
        updatedAt = mock())
  }

  fun protocolDrug(
      name: String = "drug name",
      dosages: List<String> = listOf("5mg", "10mg")
  ): ProtocolDrug {
    return ProtocolDrug(
        name = name,
        rxNormCode = "rxnormcode-1",
        dosages = dosages)
  }

  fun appointment(): Appointment {
    return Appointment(
        uuid = mock(),
        patientUuid = mock(),
        scheduledDate = LocalDate.now(ZoneOffset.UTC).minusDays(10),
        facilityUuid = mock(),
        status = Appointment.Status.SCHEDULED,
        cancelReason = Appointment.CancelReason.PATIENT_NOT_RESPONDING,
        syncStatus = mock(),
        agreedToVisit = null,
        remindOn = LocalDate.now(ZoneOffset.UTC).minusDays(2),
        createdAt = mock(),
        updatedAt = mock()
    )
  }

  fun overdueAppointment(
      name: String = "somebody"
  ): OverdueAppointment {
    return OverdueAppointment(
        fullName = name,
        gender = mock(),
        dateOfBirth = LocalDate.now(ZoneOffset.UTC).minusYears(30),
        age = null,
        phoneNumber = mock(),
        appointment = appointment(),
        bloodPressure = BloodPressureMeasurement(
            uuid = mock(),
            systolic = 175,
            diastolic = 77,
            createdAt = mock(),
            updatedAt = Instant.now(),
            syncStatus = SyncStatus.PENDING,
            userUuid = mock(),
            facilityUuid = mock(),
            patientUuid = mock())
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
      hasHadHeartAttack: Boolean = true,
      hasHadStroke: Boolean = false,
      hasHadKidneyDisease: Boolean = true,
      diagnosedWithHypertension: Boolean = false,
      isOnTreatmentForHypertension: Boolean = false,
      hasDiabetes: Boolean = true,
      updatedAt: Instant = Instant.now()
  ): MedicalHistory {
    return MedicalHistory(
        uuid = UUID.randomUUID(),
        patientUuid = UUID.randomUUID(),
        hasHadHeartAttack = hasHadHeartAttack,
        hasHadStroke = hasHadStroke,
        hasHadKidneyDisease = hasHadKidneyDisease,
        diagnosedWithHypertension = diagnosedWithHypertension,
        isOnTreatmentForHypertension = isOnTreatmentForHypertension,
        hasDiabetes = hasDiabetes,
        syncStatus = SyncStatus.PENDING,
        createdAt = Instant.now(),
        updatedAt = updatedAt)
  }
}
