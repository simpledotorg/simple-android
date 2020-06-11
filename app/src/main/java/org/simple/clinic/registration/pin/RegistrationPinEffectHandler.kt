package org.simple.clinic.registration.pin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession

class RegistrationPinEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    @Assisted private val uiActions: RegistrationPinUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationPinUiActions): RegistrationPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationPinEffect, RegistrationPinEvent> {
    return RxMobius.subtypeEffectHandler<RegistrationPinEffect, RegistrationPinEvent>()
        .addTransformer(SaveCurrentOngoingEntry::class.java, saveCurrentOngoingEntry())
        .build()
  }

  private fun saveCurrentOngoingEntry(): ObservableTransformer<SaveCurrentOngoingEntry, RegistrationPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { userSession.saveOngoingRegistrationEntry(it.entry) }
          .map { CurrentOngoingEntrySaved }
    }
  }
}
