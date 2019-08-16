package org.simple.clinic.di

import io.reactivex.Single
import org.simple.clinic.patient.PatientModule
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.fuzzy.AbsoluteFuzzer
import org.simple.clinic.patient.fuzzy.AgeFuzzer
import org.simple.clinic.util.UtcClock
import java.util.UUID

class TestPatientModule : PatientModule() {
  override fun provideAgeFuzzer(utcClock: UtcClock): AgeFuzzer {
    val numberOfYearsToFuzzBy = 5
    return AbsoluteFuzzer(utcClock, numberOfYearsToFuzzBy)
  }

  override fun provideFilterPatientByName(): SearchPatientByName {
    return object : SearchPatientByName {
      override fun search(searchTerm: String, names: List<PatientSearchResult.PatientNameAndId>): Single<List<UUID>> {
        val results = names
            .filter { it.fullName.contains(other = searchTerm, ignoreCase = true) }
            .map { it.uuid }

        return Single.just(results)
      }
    }
  }
}
