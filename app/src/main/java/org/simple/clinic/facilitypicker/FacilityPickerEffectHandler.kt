package org.simple.clinic.facilitypicker

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilityPickerEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: FacilityPickerUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun inject(uiActions: FacilityPickerUiActions): FacilityPickerEffectHandler
  }

  fun build(): ObservableTransformer<FacilityPickerEffect, FacilityPickerEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilityPickerEffect, FacilityPickerEvent>()
        .build()
  }
}
