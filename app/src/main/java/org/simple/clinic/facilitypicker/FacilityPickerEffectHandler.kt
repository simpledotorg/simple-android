package org.simple.clinic.facilitypicker

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilityPickerEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val screenLocationUpdates: ScreenLocationUpdates,
    private val facilityRepository: FacilityRepository,
    private val appConfigRepository: AppConfigRepository,
    @Assisted private val uiActions: FacilityPickerUiActions
) {

  @AssistedFactory
  interface Factory {
    fun inject(uiActions: FacilityPickerUiActions): FacilityPickerEffectHandler
  }

  fun build(): ObservableTransformer<FacilityPickerEffect, FacilityPickerEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilityPickerEffect, FacilityPickerEvent>()
        .addTransformer(FetchCurrentLocation::class.java, fetchLocation())
        .addTransformer(LoadFacilitiesWithQuery::class.java, loadFacilitiesWithQuery())
        .addConsumer(ForwardSelectedFacility::class.java, { uiActions.dispatchSelectedFacility(it.facility) }, schedulers.ui())
        .addTransformer(LoadFacilitiesInCurrentGroup::class.java, loadFacilitiesInCurrentGroup())
        .build()
  }

  private fun fetchLocation(): ObservableTransformer<FetchCurrentLocation, FacilityPickerEvent> {
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

  private fun loadFacilitiesWithQuery(): ObservableTransformer<LoadFacilitiesWithQuery, FacilityPickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            facilityRepository
                .facilities(searchQuery = effect.query)
                .subscribeOn(schedulers.io())
                .map(::filterFacilitiesByState)
                .map { FacilitiesFetched(query = effect.query, facilities = it) }
          }
    }
  }

  private fun loadFacilitiesInCurrentGroup(): ObservableTransformer<LoadFacilitiesInCurrentGroup, FacilityPickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { effect ->
            facilityRepository
                .facilitiesInCurrentGroup(effect.query)
                .map { FacilitiesFetched(query = effect.query, facilities = it) }
          }
    }
  }

  private fun filterFacilitiesByState(facilities: List<Facility>): List<Facility> {
    val currentState = appConfigRepository.currentState()
    return if (currentState != null) {
      facilities.filter { it.state == currentState }
    } else {
      facilities
    }
  }
}
