package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.DoesNotMatchEnteredPin
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.Valid
import org.simple.clinic.user.OngoingRegistrationEntry

class RegistrationConfirmPinEffectHandler @AssistedInject constructor(
    @Assisted private val viewEffectsConsumer: Consumer<RegistrationConfirmPinViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<RegistrationConfirmPinViewEffect>
    ): RegistrationConfirmPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent>()
        .addTransformer(ValidatePinConfirmation::class.java, validatePinConfirmation())
        .addConsumer(RegistrationConfirmPinViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun validatePinConfirmation(): ObservableTransformer<ValidatePinConfirmation, RegistrationConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { (pinConfirmation, entry) -> checkIfConfirmedPinIsValid(pinConfirmation, entry) }
          .map(::PinConfirmationValidated)
    }
  }

  private fun checkIfConfirmedPinIsValid(
      pinConfirmation: String,
      entry: OngoingRegistrationEntry
  ): RegistrationConfirmPinValidationResult {
    val isPinConfirmationSameAsEnteredPin = pinConfirmation == entry.pin!!

    return if (isPinConfirmationSameAsEnteredPin)
      Valid
    else
      DoesNotMatchEnteredPin
  }
}
