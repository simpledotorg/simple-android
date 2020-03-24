package org.simple.clinic.security.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Allowed
import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState.Blocked

class PinEntryUpdate(
    private val submitPinAtLength: Int
) : Update<PinEntryModel, PinEntryEvent, PinEntryEffect> {

  override fun update(model: PinEntryModel, event: PinEntryEvent): Next<PinEntryModel, PinEntryEffect> {
    return when (event) {
      is PinTextChanged -> next(model.enteredPinChanged(event.pin))
      is PinDigestToVerify -> next(model.updatePinDigest(event.pinDigest))
      is PinEntryStateChanged -> {
        val effect = when (val protectedState = event.state) {
          is Allowed -> generateEffectForAllowingPinEntry(protectedState)
          is Blocked -> ShowIncorrectPinLimitReachedError(protectedState.attemptsMade)
        }

        dispatch(effect)
      }
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
}
