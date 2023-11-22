package org.simple.clinic.facility.alertchange

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEffect.LoadIsFacilityChangedStatus
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.IsFacilityChangedStatusLoaded
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Named

class AlertFacilityChangeEffectHandler @AssistedInject constructor(
    @Named("is_facility_switched")
    private val isFacilitySwitchedPreference: Preference<Boolean>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<AlertFacilityChangeViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<AlertFacilityChangeViewEffect>) : AlertFacilityChangeEffectHandler
  }

  fun build(): ObservableTransformer<AlertFacilityChangeEffect, AlertFacilityChangeEvent> {
    return RxMobius
        .subtypeEffectHandler<AlertFacilityChangeEffect, AlertFacilityChangeEvent>()
        .addTransformer(LoadIsFacilityChangedStatus::class.java, loadFacilityChangedStatus())
        .addConsumer(AlertFacilityChangeViewEffect::class.java, viewEffectsConsumer::accept)
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
