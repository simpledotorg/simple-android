package org.simple.clinic.registration.register

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.User
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationLoadingEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val registerUser: RegisterUser,
    private val currentUser: Lazy<User>,
    @Assisted private val uiActions: RegistrationLoadingUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationLoadingUiActions): RegistrationLoadingEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationLoadingEffect, RegistrationLoadingEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationLoadingEffect, RegistrationLoadingEvent>()
        .addTransformer(LoadRegistrationDetails::class.java, loadRegistrationDetails())
        .addTransformer(RegisterUserAtFacility::class.java, registerUserAtFacility())
        .addAction(GoToHomeScreen::class.java, uiActions::openHomeScreen, schedulers.ui())
        .build()
  }

  private fun loadRegistrationDetails(): ObservableTransformer<LoadRegistrationDetails, RegistrationLoadingEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap {
            val details = RegistrationDetailsLoaded(
                user = currentUser.get()
            )

            Observable.just(details)
          }
    }
  }

  private fun registerUserAtFacility(): ObservableTransformer<RegisterUserAtFacility, RegistrationLoadingEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMapSingle { effect ->
            registerUser
                .registerUserAtFacility(effect.user)
                .subscribeOn(schedulers.io())
                .map { UserRegistrationCompleted(RegisterUserResult.from(it)) }
          }
    }
  }
}
