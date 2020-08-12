package org.simple.clinic.main

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class TheActivityEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: TheActivityUiActions
) {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(uiActions: TheActivityUiActions): TheActivityEffectHandler
  }

  fun build(): ObservableTransformer<TheActivityEffect, TheActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<TheActivityEffect, TheActivityEvent>()
        .build()
  }
}
