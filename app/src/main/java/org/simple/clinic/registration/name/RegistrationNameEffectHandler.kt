package org.simple.clinic.registration.name

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Blank
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Valid
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationNameEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    @Assisted private val uiActions: RegistrationNameUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationNameUiActions): RegistrationNameEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationNameEffect, RegistrationNameEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationNameEffect, RegistrationNameEvent>()
        .addConsumer(PrefillFields::class.java, { uiActions.preFillUserDetails(it.entry) }, schedulers.ui())
        .addTransformer(ValidateEnteredName::class.java, validateNameEntry())
        .addTransformer(SaveCurrentRegistrationEntry::class.java, saveCurrentRegistrationEntry())
        .build()
  }

  private fun validateNameEntry(): ObservableTransformer<ValidateEnteredName, RegistrationNameEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { if (it.name.isBlank()) Blank else Valid }
          .map(::NameValidated)
    }
  }

  private fun saveCurrentRegistrationEntry(): ObservableTransformer<SaveCurrentRegistrationEntry, RegistrationNameEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { userSession.saveOngoingRegistrationEntry(it.entry) }
          .map { CurrentRegistrationEntrySaved }
    }
  }
}
