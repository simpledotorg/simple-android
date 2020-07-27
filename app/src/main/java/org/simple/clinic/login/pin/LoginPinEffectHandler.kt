package org.simple.clinic.login.pin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class LoginPinEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val userSession: UserSession,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): LoginPinEffectHandler
  }

  fun build(): ObservableTransformer<LoginPinEffect, LoginPinEvent> = RxMobius
      .subtypeEffectHandler<LoginPinEffect, LoginPinEvent>()
      .addTransformer(LoadOngoingLoginEntry::class.java, loadOngoingLoginEntry())
      .addTransformer(SaveOngoingLoginEntry::class.java, saveOngoingLoginEntry())
      .build()

  private fun saveOngoingLoginEntry(): ObservableTransformer<SaveOngoingLoginEntry, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { (ongoingLoginEntry) ->
            userSession
                .saveOngoingLoginEntry(ongoingLoginEntry)
                .andThen(Single.just(LoginPinScreenUpdatedLoginEntry(ongoingLoginEntry)))
          }
    }
  }

  private fun loadOngoingLoginEntry(): ObservableTransformer<LoadOngoingLoginEntry, LoginPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { userSession.ongoingLoginEntry() }
          .map(::OngoingLoginEntryLoaded)
    }
  }
}
