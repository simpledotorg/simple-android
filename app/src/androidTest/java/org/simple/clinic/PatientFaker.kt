package org.simple.clinic

import io.bloco.faker.Faker
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class PatientFaker @Inject constructor(private val faker: Faker) {

  fun patientPayload(
      fullName: String = faker.name.name(),
      gender: Gender = Gender.values().toList().shuffled().first(),
      age: Int? = Math.random().times(100).toInt(),
      ageUpdatedAt: Instant? = Instant.now(),
      status: PatientStatus = PatientStatus.values().toList().shuffled().first(),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now(),
      address: PatientAddressPayload = addressPayload(),
      phoneNumber: PatientPhoneNumberPayload = phoneNumberPayload()
  ): PatientPayload {
    return PatientPayload(
        uuid = UUID.randomUUID(),
        fullName = fullName,
        gender = gender,
        dateOfBirth = null,
        age = age,
        ageUpdatedAt = ageUpdatedAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        address = address,
        phoneNumbers = listOf(phoneNumber)
    )
  }

  fun addressPayload(): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = UUID.randomUUID(),
        colonyOrVillage = faker.address.streetAddress(),
        district = faker.address.city(),
        state = faker.address.state(),
        country = faker.address.country(),
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }

  fun phoneNumberPayload(): PatientPhoneNumberPayload {
    return PatientPhoneNumberPayload(
        uuid = UUID.randomUUID(),
        number = faker.phoneNumber.phoneNumber(),
        type = PatientPhoneNumberType.values().toList().shuffled().first(),
        active = true,
        createdAt = Instant.now(),
        updatedAt = Instant.now())
  }
}
