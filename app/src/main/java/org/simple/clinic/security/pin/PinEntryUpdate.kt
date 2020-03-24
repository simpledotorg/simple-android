package org.simple.clinic.security.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked

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

        next(updatedModel, generateEffectsForPinSubmission(updatedModel))
      }
      is PinEntryStateChanged -> {
        val effects = when (val protectedState = event.state) {
          is Allowed -> setOf(generateEffectForAllowingPinEntry(protectedState), AllowPinEntry)
          is Blocked -> setOf(ShowIncorrectPinLimitReachedError(protectedState.attemptsMade), BlockPinEntryUntil(protectedState.blockedTill))
        }

        Next.dispatch(effects)
      }
      is CorrectPinEntered -> dispatch(RecordSuccessfulAttempt)
      is WrongPinEntered -> dispatch(AllowPinEntry, RecordFailedAttempt)
      else -> noChange()
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
    return model.hasPinDigestBeenLoaded && model.enteredPin.length == submitPinAtLength
  }

  private fun generateEffectsForPinSubmission(model: PinEntryModel): Set<PinEntryEffect> {
    val effects = mutableSetOf<PinEntryEffect>()

    if(isReadyToSubmitPin(model)) {
      effects.apply {
        add(ValidateEnteredPin(model.enteredPin, model.pinDigestToVerify!!))
        add(HideError)
        add(ShowProgress)
      }
    }

    return effects
  }
}
