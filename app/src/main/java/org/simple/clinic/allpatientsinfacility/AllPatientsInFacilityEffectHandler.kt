package org.simple.clinic.allpatientsinfacility

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class AllPatientsInFacilityEffectHandler @Inject constructor(
    private val currentFacility: Lazy<Facility>,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<AllPatientsInFacilityEffect, AllPatientsInFacilityEvent> {
    return RxMobius
        .subtypeEffectHandler<AllPatientsInFacilityEffect, AllPatientsInFacilityEvent>()
        .addTransformer(FetchFacilityEffect::class.java, fetchFacilityEffectHandler())
        .addTransformer(FetchPatientsEffect::class.java, fetchPatientsEffectHandler())
        .build()
  }

  private fun fetchFacilityEffectHandler(): ObservableTransformer<FetchFacilityEffect, AllPatientsInFacilityEvent> {
    return ObservableTransformer { facilityStream ->
      facilityStream
          .observeOn(schedulersProvider.io())
          .map { currentFacility.get() }
          .map(::FacilityFetchedEvent)
    }
  }

  private fun fetchPatientsEffectHandler(): ObservableTransformer<FetchPatientsEffect, AllPatientsInFacilityEvent> {
    return ObservableTransformer { fetchPatients ->
      fetchPatients
          .map { it.facility }
          .switchMap { facility -> loadSearchResultsFromRepository(patientRepository, facility, schedulersProvider) }
          .map(::mapSearchResultsToUiStates)
          .map { patients -> if (patients.isEmpty()) NoPatientsInFacilityEvent else HasPatientsInFacilityEvent(patients) }
    }
  }

  private fun loadSearchResultsFromRepository(
      patientRepository: PatientRepository,
      facility: Facility,
      schedulersProvider: SchedulersProvider
  ): Observable<List<PatientSearchResult>> {
    return patientRepository
        .allPatientsInFacility_Old(facility)
        .subscribeOn(schedulersProvider.io())
  }

  private fun mapSearchResultsToUiStates(
      searchResults: List<PatientSearchResult>
  ): List<PatientSearchResultUiState> {
    return searchResults.map(::PatientSearchResultUiState)
  }
}
