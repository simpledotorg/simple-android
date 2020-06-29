package org.simple.clinic.registration.facility

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.location.ScreenLocationUpdates
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationFacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val screenLocationUpdates: ScreenLocationUpdates,
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
}
