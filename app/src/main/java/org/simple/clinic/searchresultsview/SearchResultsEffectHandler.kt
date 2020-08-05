package org.simple.clinic.searchresultsview

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
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
    val currentFacilityStream = Observable
        .fromCallable { currentFacility.get() }
        .subscribeOn(schedulers.io())
        .take(1)

    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            patientRepository
                .search(effect.searchCriteria)
                .subscribeOn(schedulers.io())
                .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao, currentFacilityStream))
                .map(::SearchResultsLoaded)
          }
    }
  }
}
