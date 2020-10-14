package org.simple.clinic.security.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.NetworkError
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.OtherError
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.ServerError

class PinEntryUpdate(
    private val submitPinAtLength: Int
) : Update<PinEntryModel, PinEntryEvent, PinEntryEffect> {

  override fun update(model: PinEntryModel, event: PinEntryEvent): Next<PinEntryModel, PinEntryEffect> {
    return when (event) {
      is PinTextChanged -> {
        val updatedModel = model.enteredPinChanged(event.pin)

        next(updatedModel, generateEffectsForPinSubmission(updatedModel))
      }
      is PinEntryStateChanged -> Next.dispatch(effectsForStateChange(event.state))
      is PinVerified -> {
        when (event.result) {
          is Correct -> dispatch(RecordSuccessfulAttempt, CorrectPinEntered(event.result.data))
          is Incorrect -> dispatch(AllowPinEntry, RecordFailedAttempt, ClearPin)
          // Will be filled in later when we implement PIN verification
          // via the server API call.
          is NetworkError -> dispatch(AllowPinEntry, ShowNetworkError)
          is ServerError -> dispatch(AllowPinEntry, ShowServerError)
          is OtherError -> dispatch(AllowPinEntry, ShowUnexpectedError)
        }
      }
      is PinAuthenticated -> noChange()
      PinEntryDoneClicked -> noChange()
    }
  }

  private fun effectsForStateChange(protectedState: BruteForceProtection.ProtectedState): Set<PinEntryEffect> {
    return when (protectedState) {
      is Allowed -> setOf(generateEffectForAllowingPinEntry(protectedState), AllowPinEntry)
      is Blocked -> setOf(ShowIncorrectPinLimitReachedError(protectedState.attemptsMade), BlockPinEntryUntil(protectedState.blockedTill))
    }
  }

  private fun generateEffectForAllowingPinEntry(protectedState: Allowed): PinEntryEffect {
    return if (protectedState.attemptsMade == 0) {
      HideError
    } else {
      ShowIncorrectPinError(protectedState.attemptsMade, protectedState.attemptsRemaining)
    }
  }

  private fun isReadyToSubmitPin(model: PinEntryModel): Boolean {
    return model.enteredPin.length == submitPinAtLength
  }

  private fun generateEffectsForPinSubmission(model: PinEntryModel): Set<PinEntryEffect> {
    val effects = mutableSetOf<PinEntryEffect>()

    if (isReadyToSubmitPin(model)) {
      effects.apply {
        add(VerifyPin(model.enteredPin))
        add(HideError)
        add(ShowProgress)
      }
    }

    return effects
  }
}
