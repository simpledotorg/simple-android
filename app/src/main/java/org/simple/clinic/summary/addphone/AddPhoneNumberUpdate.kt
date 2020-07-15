package org.simple.clinic.summary.addphone

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class AddPhoneNumberUpdate : Update<AddPhoneNumberModel, AddPhoneNumberEvent, AddPhoneNumberEffect> {

  override fun update(model: AddPhoneNumberModel, event: AddPhoneNumberEvent): Next<AddPhoneNumberModel, AddPhoneNumberEffect> {
    return noChange()
  }
}
