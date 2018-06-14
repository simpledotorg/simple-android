package org.resolvetosavelives.red.patient

import com.nhaarman.mockito_kotlin.mock
import org.resolvetosavelives.red.bp.BloodPressureMeasurement
import java.util.UUID

/**
 * Test data generator. Name inspired from the Faker library.
 */
object PatientFaker {

  fun patient(
      uuid: UUID = UUID.randomUUID(),
      addressUuid: UUID = UUID.randomUUID(),
      fullName: String = "name"
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
        syncStatus = mock())
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
      patientUuid: UUID = mock(),
      systolic: Int = 164,
      diastolic: Int = 90
  ): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = mock(),
        systolic = systolic,
        diastolic = diastolic,
        createdAt = mock(),
        updatedAt = mock(),
        syncStatus = mock(),
        patientUuid = patientUuid
    )
  }
}
