package org.simple.clinic.util

import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.threeten.bp.Duration
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
