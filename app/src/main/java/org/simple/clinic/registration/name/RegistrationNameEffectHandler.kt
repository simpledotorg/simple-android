package org.simple.clinic.registration.name

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Blank
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Valid
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationNameEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: RegistrationNameUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: RegistrationNameUiActions): RegistrationNameEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationNameEffect, RegistrationNameEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationNameEffect, RegistrationNameEvent>()
        .addConsumer(PrefillFields::class.java, { uiActions.preFillUserDetails(it.entry) }, schedulers.ui())
        .addTransformer(ValidateEnteredName::class.java, validateNameEntry())
        .addConsumer(ProceedToPinEntry::class.java, { uiActions.openRegistrationPinEntryScreen(it.entry) }, schedulers.ui())
        .build()
  }

  private fun validateNameEntry(): ObservableTransformer<ValidateEnteredName, RegistrationNameEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { if (it.name.isBlank()) Blank else Valid }
          .map(::NameValidated)
    }
  }
}
