package org.simple.clinic.forgotpin.createnewpin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class ForgotPinCreateNewEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions) : ForgotPinCreateNewEffectHandler
  }

  fun build(): ObservableTransformer<ForgotPinCreateNewEffect, ForgotPinCreateNewEvent> = RxMobius
      .subtypeEffectHandler<ForgotPinCreateNewEffect, ForgotPinCreateNewEvent>()
      .build()
}
