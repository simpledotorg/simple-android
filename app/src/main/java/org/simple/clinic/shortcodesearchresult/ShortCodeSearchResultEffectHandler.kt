package org.simple.clinic.shortcodesearchresult

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class ShortCodeSearchResultEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): ShortCodeSearchResultEffectHandler
  }

  fun build(): ObservableTransformer<ShortCodeSearchResultEffect, ShortCodeSearchResultEvent> {
    return RxMobius
        .subtypeEffectHandler<ShortCodeSearchResultEffect, ShortCodeSearchResultEvent>()
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientId)}, schedulers.ui())
        .build()
  }
}
