package org.simple.clinic.util

import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.threeten.bp.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

fun <T> TestObserver<T>.assertLatestValue(value: T) {
  @Suppress("UnstableApiUsage")
  assertValueAt(valueCount() - 1, value)
}

fun randomMedicalHistoryAnswer(): Answer {
  return Answer.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun randomGender(): Gender {
  return Gender.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun randomPatientPhoneNumberType(): PatientPhoneNumberType {
  return PatientPhoneNumberType.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun TestScheduler.advanceTimeBy(duration: Duration) {
  advanceTimeBy(duration.toMillis(), TimeUnit.MILLISECONDS)
}

fun LoggedInUserPayload.toUser(loggedInStatus: User.LoggedInStatus): User {
  return User(
      uuid = uuid,
      fullName = fullName,
      phoneNumber = phoneNumber,
      pinDigest = pinDigest,
      status = status,
      createdAt = createdAt,
      updatedAt = updatedAt,
      loggedInStatus = loggedInStatus
  )
}

fun User.toPayload(registrationFacilityUuid: UUID): LoggedInUserPayload {
  return LoggedInUserPayload(
      uuid = uuid,
      fullName = fullName,
      phoneNumber = phoneNumber,
      pinDigest = pinDigest,
      registrationFacilityId = registrationFacilityUuid,
      status = status,
      createdAt = createdAt,
      updatedAt = updatedAt
  )
}
