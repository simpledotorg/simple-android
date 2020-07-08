package org.simple.clinic.scheduleappointment.facilityselection

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: FacilitySelectionUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: FacilitySelectionUiActions): FacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<FacilitySelectionEffect, FacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilitySelectionEffect, FacilitySelectionEvent>()
        .build()
  }
}
