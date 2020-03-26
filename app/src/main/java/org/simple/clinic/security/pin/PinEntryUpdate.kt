package org.simple.clinic.security.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked
import org.simple.clinic.security.pin.verification.PinVerificationMethod
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Failure
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect

class PinEntryUpdate(
    private val submitPinAtLength: Int
) : Update<PinEntryModel, PinEntryEvent, PinEntryEffect> {

  override fun update(model: PinEntryModel, event: PinEntryEvent): Next<PinEntryModel, PinEntryEffect> {
    return when (event) {
      is PinTextChanged -> {
        val updatedModel = model.enteredPinChanged(event.pin)

        next(updatedModel, generateEffectsForPinSubmission(updatedModel))
      }
      is PinDigestToVerify -> {
        val updatedModel = model.updatePinDigest(event.pinDigest)

        next(updatedModel)
      }
      is PinEntryStateChanged -> Next.dispatch(effectsForStateChange(event.state))
      is CorrectPinEntered -> dispatch(RecordSuccessfulAttempt, DispatchPinVerified(model.enteredPin))
      is WrongPinEntered -> dispatch(AllowPinEntry, RecordFailedAttempt, ClearPin)
      is PinVerified -> {
        when(event.result) {
          is Correct -> dispatch(RecordSuccessfulAttempt, DispatchCorrectPinEntered(event.result.data))
          is Incorrect -> dispatch(AllowPinEntry, RecordFailedAttempt, ClearPin)
          // Will be filled in later when we implement PIN verification
          // via the server API call.
          is Failure -> noChange()
        }
      }
      is PinAuthenticated -> noChange()
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
