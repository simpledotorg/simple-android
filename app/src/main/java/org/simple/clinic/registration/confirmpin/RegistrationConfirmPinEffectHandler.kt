package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.DoesNotMatchEnteredPin
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.Valid
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationConfirmPinEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: RegistrationConfirmPinUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationConfirmPinUiActions): RegistrationConfirmPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent>()
        .addTransformer(ValidatePinConfirmation::class.java, validatePinConfirmation())
        .addAction(ClearPin::class.java, uiActions::clearPin, schedulers.ui())
        .build()
  }

  private fun validatePinConfirmation(): ObservableTransformer<ValidatePinConfirmation, RegistrationConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { (pinConfirmation, entry) -> pinConfirmation != entry.pin!! }
          .map { isPinConfirmationSameAsEnteredPin -> if (isPinConfirmationSameAsEnteredPin) Valid else DoesNotMatchEnteredPin }
          .map(::PinConfirmationValidated)
    }
  }
}
