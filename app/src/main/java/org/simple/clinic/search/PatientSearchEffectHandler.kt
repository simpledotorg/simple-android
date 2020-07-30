package org.simple.clinic.search

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientSearchEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: PatientSearchUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: PatientSearchUiActions): PatientSearchEffectHandler
  }

  fun build(): ObservableTransformer<PatientSearchEffect, PatientSearchEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSearchEffect, PatientSearchEvent>()
        .build()
  }
}
