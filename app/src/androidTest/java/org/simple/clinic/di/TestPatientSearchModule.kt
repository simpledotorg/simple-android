package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.filter.SearchPatientByName
import java.util.UUID

@Module
class TestPatientSearchModule {

  @Provides
  fun provideFilterPatientByName(): SearchPatientByName {
    return object : SearchPatientByName {
      override fun search(searchTerm: String, names: List<PatientSearchResult.PatientNameAndId>): List<UUID> {
        val results = names
            .filter { it.fullName.contains(other = searchTerm, ignoreCase = true) }
            .map { it.uuid }

        return results
      }
    }
  }
}
