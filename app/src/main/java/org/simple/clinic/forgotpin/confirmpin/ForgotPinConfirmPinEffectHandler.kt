package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class ForgotPinConfirmPinEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): ForgotPinConfirmPinEffectHandler
  }

  fun build(): ObservableTransformer<ForgotPinConfirmPinEffect, ForgotPinConfirmPinEvent> = RxMobius
      .subtypeEffectHandler<ForgotPinConfirmPinEffect, ForgotPinConfirmPinEvent>()
      .addTransformer(LoadLoggedInUser::class.java, loadLoggedInUser())
      .build()

  private fun loadLoggedInUser(): ObservableTransformer<LoadLoggedInUser, ForgotPinConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { userSession.loggedInUser() }
          .extractIfPresent()
          .map(::LoggedInUserLoaded)
    }
  }
}
