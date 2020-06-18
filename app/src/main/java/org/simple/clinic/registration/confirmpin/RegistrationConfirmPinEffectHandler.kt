package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.DoesNotMatchEnteredPin
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.Valid
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.Instant

class RegistrationConfirmPinEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
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
        .addTransformer(SaveCurrentRegistrationEntry::class.java, saveCurrentRegistrationEntry())
        .addAction(OpenFacilitySelectionScreen::class.java, uiActions::openFacilitySelectionScreen, schedulers.ui())
        .build()
  }

  private fun validatePinConfirmation(): ObservableTransformer<ValidatePinConfirmation, RegistrationConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { (pinConfirmation, entry) -> checkIfConfirmedPinIsValid(pinConfirmation, entry) }
          .map { PinConfirmationValidated(it, Instant.now(utcClock)) }
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

  private fun saveCurrentRegistrationEntry(): ObservableTransformer<SaveCurrentRegistrationEntry, RegistrationConfirmPinEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { userSession.saveOngoingRegistrationEntry(it.entry) }
          .map { RegistrationEntrySaved }
    }
  }
}
