package org.simple.clinic.scheduleappointment.facilityselection

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: FacilitySelectionUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: FacilitySelectionUiActions): FacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<FacilitySelectionEffect, FacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilitySelectionEffect, FacilitySelectionEvent>()
        .addConsumer(ForwardSelectedFacility::class.java, { uiActions.sendSelectedFacility(it.facility) }, schedulers.ui())
        .build()
  }
}
