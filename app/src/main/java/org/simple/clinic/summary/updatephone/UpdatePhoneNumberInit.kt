package org.simple.clinic.summary.updatephone

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class UpdatePhoneNumberInit : Init<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {

  override fun init(model: UpdatePhoneNumberModel): First<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {
    return first(model)
  }
}
