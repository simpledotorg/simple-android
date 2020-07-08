package org.simple.clinic.facility.change

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilityChangeEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: FacilityChangeUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: FacilityChangeUiActions): FacilityChangeEffectHandler
  }

  fun build(): ObservableTransformer<FacilityChangeEffect, FacilityChangeEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilityChangeEffect, FacilityChangeEvent>()
        .build()
  }
}
