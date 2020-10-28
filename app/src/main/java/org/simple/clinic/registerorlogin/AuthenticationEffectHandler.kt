package org.simple.clinic.registerorlogin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class AuthenticationEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: AuthenticationUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: AuthenticationUiActions): AuthenticationEffectHandler
  }

  fun build(): ObservableTransformer<AuthenticationEffect, AuthenticationEvent> {
    return RxMobius
        .subtypeEffectHandler<AuthenticationEffect, AuthenticationEvent>()
        .addAction(OpenCountrySelectionScreen::class.java, uiActions::openCountrySelectionScreen, schedulers.ui())
        .addAction(OpenRegistrationPhoneScreen::class.java, uiActions::openRegistrationPhoneScreen, schedulers.ui())
        .build()
  }
}
