package org.simple.clinic.security.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
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
        val effects = when (val protectedState = event.state) {
          is Allowed -> setOf(generateEffectForAllowingPinEntry(protectedState), AllowPinEntry)
          is Blocked -> setOf(ShowIncorrectPinLimitReachedError(protectedState.attemptsMade), BlockPinEntryUntil(protectedState.blockedTill))
        }

        dispatch(effects)
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
