package org.simple.clinic.searchresultsview

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class SearchResultsEffectHandler @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao
) {

  fun build(): ObservableTransformer<SearchResultsEffect, SearchResultsEvent> {
    return RxMobius
        .subtypeEffectHandler<SearchResultsEffect, SearchResultsEvent>()
        .addTransformer(SearchWithCriteria::class.java, searchForPatients())
        .build()
  }

  private fun searchForPatients(): ObservableTransformer<SearchWithCriteria, SearchResultsEvent> {
    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .subscribeOn(schedulers.io())
        .switchMap(facilityRepository::currentFacility)
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
