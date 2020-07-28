package org.simple.clinic.home.overdue

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: OverdueUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: OverdueUiActions): OverdueEffectHandler
  }

  fun build(): ObservableTransformer<OverdueEffect, OverdueEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueEffect, OverdueEvent>()
        .build()
  }
}
