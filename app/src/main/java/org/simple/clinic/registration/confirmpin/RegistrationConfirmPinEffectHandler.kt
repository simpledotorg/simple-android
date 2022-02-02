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
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationConfirmPinEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: RegistrationConfirmPinUiActions,
    @Assisted private val viewEffectsConsumer: Consumer<RegistrationConfirmPinViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: RegistrationConfirmPinUiActions,
        viewEffectsConsumer: Consumer<RegistrationConfirmPinViewEffect>
    ): RegistrationConfirmPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent>()
        .addTransformer(ValidatePinConfirmation::class.java, validatePinConfirmation())
        .addConsumer(OpenFacilitySelectionScreen::class.java, { uiActions.openFacilitySelectionScreen(it.entry) }, schedulers.ui())
        .addConsumer(GoBackToPinEntry::class.java, { uiActions.goBackToPinScreen(it.entry) }, schedulers.ui())
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
