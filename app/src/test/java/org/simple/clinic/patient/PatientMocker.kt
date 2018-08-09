package org.simple.clinic.patient

import com.nhaarman.mockito_kotlin.mock
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.user.LoggedInUser
import java.util.UUID

/**
 * Generates test data.
 */
object PatientMocker {

  fun patient(
      uuid: UUID = UUID.randomUUID(),
      addressUuid: UUID = UUID.randomUUID(),
      fullName: String = "name",
      syncStatus: SyncStatus = mock()
  ): Patient {
    return Patient(
        uuid = uuid,
        addressUuid = addressUuid,
        fullName = fullName,
        searchableName = fullName,
        gender = mock(),
        dateOfBirth = mock(),
        age = mock(),
        status = mock(),
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
      uuid: UUID = mock(),
      patientUuid: UUID = mock(),
      systolic: Int = 164,
      diastolic: Int = 90,
      syncStatus: SyncStatus = mock()
  ): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = uuid,
        systolic = systolic,
        diastolic = diastolic,
        createdAt = mock(),
        updatedAt = mock(),
        syncStatus = syncStatus,
        userUuid = mock(),
        facilityUuid = mock(),
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
        UUID.fromString("feab6950-86fe-4b70-95c9-f21620140068"),
        name = name,
        rxNormCode = "rxnormcode-1",
        dosages = dosages,
        protocolUUID = mock())
  }

  fun loggedInUser(
      uuid: UUID = mock(),
      name: String = "a name",
      phone: String = "a phone",
      pinDigest: String = "a hash",
      status: LoggedInUser.Status = LoggedInUser.Status.WAITING_FOR_APPROVAL
  ): LoggedInUser {
    return LoggedInUser(
        uuid = uuid,
        fullName = name,
        phoneNumber = phone,
        pinDigest = pinDigest,
        facilityUuids = mock(),
        createdAt = mock(),
        status = status,
        updatedAt = mock())
  }
}
