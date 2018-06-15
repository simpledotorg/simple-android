package org.resolvetosavelives.red.patient

import com.nhaarman.mockito_kotlin.mock
import org.resolvetosavelives.red.bp.BloodPressureMeasurement
import org.resolvetosavelives.red.facility.Facility
import org.resolvetosavelives.red.user.LoggedInUser
import java.util.UUID

/**
 * Test data generator. Name inspired from the Faker library.
 *
 * TODO: Rename this class since it now also generates test models other than [Patient].
 */
object PatientFaker {

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

  fun user(uuid: UUID = mock()): LoggedInUser {
    return LoggedInUser(uuid)
  }

  fun facility(
      uuid: UUID = mock(),
      district: String = "district",
      state: String = "state"
  ): Facility {
    return Facility(uuid, district, state)
  }
}
