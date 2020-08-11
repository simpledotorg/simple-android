package org.simple.clinic.summary.updatephone

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class UpdatePhoneNumberUpdate : Update<UpdatePhoneNumberModel, UpdatePhoneNumberEvent, UpdatePhoneNumberEffect> {

  override fun update(model: UpdatePhoneNumberModel, event: UpdatePhoneNumberEvent): Next<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {
    return when (event) {
      is PhoneNumberLoaded -> dispatch(PrefillPhoneNumber(event.phoneNumber))
      is PhoneNumberValidated -> noChange()
    }
  }
}
