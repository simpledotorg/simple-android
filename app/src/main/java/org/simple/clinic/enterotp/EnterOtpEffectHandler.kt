package org.simple.clinic.enterotp

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class EnterOtpEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: EnterOtpUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: EnterOtpUiActions): EnterOtpEffectHandler
  }

  fun build(): ObservableTransformer<EnterOtpEffect, EnterOtpEvent> {
    return RxMobius
        .subtypeEffectHandler<EnterOtpEffect, EnterOtpEvent>()
        .build()
  }
}
