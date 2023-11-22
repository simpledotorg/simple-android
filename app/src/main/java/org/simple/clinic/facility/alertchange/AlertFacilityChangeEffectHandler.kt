package org.simple.clinic.facility.alertchange

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEffect.LoadIsFacilityChangedStatus
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.IsFacilityChangedStatusLoaded
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject
import javax.inject.Named

class AlertFacilityChangeEffectHandler @Inject constructor(
    @Named("is_facility_switched")
    private val isFacilitySwitchedPreference: Preference<Boolean>,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<AlertFacilityChangeEffect, AlertFacilityChangeEvent> {
    return RxMobius
        .subtypeEffectHandler<AlertFacilityChangeEffect, AlertFacilityChangeEvent>()
        .addTransformer(LoadIsFacilityChangedStatus::class.java, loadFacilityChangedStatus())
        .build()
  }

  private fun loadFacilityChangedStatus(): ObservableTransformer<LoadIsFacilityChangedStatus, AlertFacilityChangeEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { IsFacilityChangedStatusLoaded(isFacilitySwitchedPreference.get()) }
    }
  }
}
