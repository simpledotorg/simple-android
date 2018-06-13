package org.resolvetosavelives.red.patient

import com.nhaarman.mockito_kotlin.mock
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
}
