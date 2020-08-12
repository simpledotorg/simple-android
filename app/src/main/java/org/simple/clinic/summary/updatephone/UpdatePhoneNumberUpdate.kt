package org.simple.clinic.summary.updatephone

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class UpdatePhoneNumberUpdate : Update<UpdatePhoneNumberModel, UpdatePhoneNumberEvent, UpdatePhoneNumberEffect> {

  override fun update(model: UpdatePhoneNumberModel, event: UpdatePhoneNumberEvent): Next<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {
    return noChange()
  }
}
