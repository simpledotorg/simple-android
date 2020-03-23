package org.simple.clinic.security.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class PinEntryUpdate: Update<PinEntryModel, PinEntryEvent, PinEntryEffect> {

  override fun update(model: PinEntryModel, event: PinEntryEvent): Next<PinEntryModel, PinEntryEffect> {
    return when(event) {
      is PinTextChanged -> next(model.enteredPinChanged(event.pin))
      else -> noChange()
    }
  }
}
