package org.simple.clinic.facilitypicker

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilityPickerEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val screenLocationUpdates: ScreenLocationUpdates,
    private val facilityRepository: FacilityRepository,
    @Assisted private val uiActions: FacilityPickerUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun inject(uiActions: FacilityPickerUiActions): FacilityPickerEffectHandler
  }

  fun build(): ObservableTransformer<FacilityPickerEffect, FacilityPickerEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilityPickerEffect, FacilityPickerEvent>()
        .addTransformer(FetchCurrentLocation::class.java, fetchLocation())
        .addTransformer(LoadFacilitiesWithQuery::class.java, loadFacilitiesWithQuery())
        .addTransformer(LoadTotalFacilityCount::class.java, loadTotalCountOfFacilities())
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
                .map { FacilitiesFetched(query = effect.query, facilities = it) }
          }
    }
  }

  private fun loadTotalCountOfFacilities(): ObservableTransformer<LoadTotalFacilityCount, FacilityPickerEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            facilityRepository
                .recordCount()
                .subscribeOn(schedulers.io())
                .take(1)
                .map(::TotalFacilityCountLoaded)
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
}
