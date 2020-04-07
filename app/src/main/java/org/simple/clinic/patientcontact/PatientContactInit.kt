package org.simple.clinic.patientcontact

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientContactInit : Init<PatientContactModel, PatientContactEffect> {

  override fun init(
      model: PatientContactModel
  ): First<PatientContactModel, PatientContactEffect> {
    return first(model)
  }
}
