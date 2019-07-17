package org.simple.clinic.util

import io.reactivex.observers.TestObserver
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Gender

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
