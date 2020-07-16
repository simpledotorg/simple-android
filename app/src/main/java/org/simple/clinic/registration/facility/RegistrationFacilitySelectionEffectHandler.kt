package org.simple.clinic.registration.facility

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant

class RegistrationFacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val screenLocationUpdates: ScreenLocationUpdates,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    @Assisted private val uiActions: RegistrationFacilitySelectionUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationFacilitySelectionUiActions): RegistrationFacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent>()
        .addTransformer(FetchCurrentLocation::class.java, fetchLocation())
        .addTransformer(LoadFacilitiesWithQuery::class.java, loadFacilitiesWithQuery())
        .addTransformer(LoadTotalFacilityCount::class.java, loadTotalCountOfFacilities())
        .addConsumer(OpenConfirmFacilitySheet::class.java, { uiActions.showConfirmFacilitySheet(it.facility.uuid, it.facility.name) }, schedulersProvider.ui())
        .addTransformer(SaveRegistrationEntryAsUser::class.java, saveCurrentRegistrationEntry())
        .addAction(MoveToIntroVideoScreen::class.java, uiActions::openIntroVideoScreen, schedulersProvider.ui())
        .build()
  }

  private fun fetchLocation(): ObservableTransformer<FetchCurrentLocation, RegistrationFacilitySelectionEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            screenLocationUpdates
                .streamUserLocation(
                    updateInterval = effect.updateInterval,
                    timeout = effect.timeout,
                    discardOlderThan = effect.discardOlderThan
                )
                .take(1)
          }
          .map(::LocationFetched)
    }
  }

  private fun loadFacilitiesWithQuery(): ObservableTransformer<LoadFacilitiesWithQuery, RegistrationFacilitySelectionEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            facilityRepository
                .facilities(searchQuery = effect.query)
                .subscribeOn(schedulersProvider.io())
                .map { FacilitiesFetched(query = effect.query, facilities = it) }
          }
    }
  }

  private fun loadTotalCountOfFacilities(): ObservableTransformer<LoadTotalFacilityCount, RegistrationFacilitySelectionEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            facilityRepository
                .recordCount()
                .subscribeOn(schedulersProvider.io())
                .take(1)
                .map(::TotalFacilityCountLoaded)
          }
    }
  }

  private fun saveCurrentRegistrationEntry(): ObservableTransformer<SaveRegistrationEntryAsUser, RegistrationFacilitySelectionEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { userSession.saveOngoingRegistrationEntry(it.entry) }
          .switchMap {
            userSession
                .saveOngoingRegistrationEntryAsUser(Instant.now(utcClock))
                .andThen(Observable.just(CurrentRegistrationEntrySaved))
          }
    }
  }
}
