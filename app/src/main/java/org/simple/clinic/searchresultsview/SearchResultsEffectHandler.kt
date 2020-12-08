package org.simple.clinic.searchresultsview

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.util.scheduler.SchedulersProvider
import timber.log.Timber
import javax.inject.Inject

class SearchResultsEffectHandler @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao,
    private val currentFacility: Lazy<Facility>
) {

  fun build(): ObservableTransformer<SearchResultsEffect, SearchResultsEvent> {
    return RxMobius
        .subtypeEffectHandler<SearchResultsEffect, SearchResultsEvent>()
        .addTransformer(SearchWithCriteria::class.java, searchForPatients())
        .build()
  }

  private fun searchForPatients(): ObservableTransformer<SearchWithCriteria, SearchResultsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .doOnNext { Timber.tag("Search").i("Search effect received") }
          .map { effect -> patientRepository.search(effect.searchCriteria) }
          .map { searchResults -> partitionSearchResultsByFacility(searchResults, currentFacility.get()) }
          .map(::SearchResultsLoaded)
    }
  }

  private fun partitionSearchResultsByFacility(
      searchResults: List<PatientSearchResult>,
      facility: Facility
  ): PatientSearchResults {
    val patientIds = searchResults.map { it.uuid }

    return PatientSearchResults.from(
        searchResults = searchResults,
        patientToFacilityIds = bloodPressureDao.patientToFacilityIds(patientIds),
        currentFacility = facility
    )
  }
}
