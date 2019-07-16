package org.simple.clinic.util

import io.reactivex.observers.TestObserver
import org.simple.clinic.medicalhistory.Answer

fun <T> TestObserver<T>.assertLatestValue(value: T) {
  @Suppress("UnstableApiUsage")
  assertValueAt(valueCount() - 1, value)
}

fun randomMedicalHistoryAnswer(): Answer {
  return Answer.TypeAdapter.knownMappings.keys.shuffled().first()
}
