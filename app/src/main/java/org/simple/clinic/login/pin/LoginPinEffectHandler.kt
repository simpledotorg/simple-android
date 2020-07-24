package org.simple.clinic.login.pin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class LoginPinEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): LoginPinEffectHandler
  }

  fun build(): ObservableTransformer<LoginPinEffect, LoginPinEvent> = RxMobius
      .subtypeEffectHandler<LoginPinEffect, LoginPinEvent>()
      .build()
}
