package org.simple.clinic.summary.updatephone

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class UpdatePhoneNumberInit : Init<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {

  override fun init(model: UpdatePhoneNumberModel): First<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {
    return first(model, LoadPhoneNumber(model.patientUuid))
  }
}
