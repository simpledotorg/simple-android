package org.simple.clinic.home.patients

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class PatientsInit : Init<PatientsModel, PatientsEffect> {

  override fun init(model: PatientsModel): First<PatientsModel, PatientsEffect> {
    return first(model, LoadUser, RefreshUserDetails, LoadNumberOfPatientsRegistered, LoadInfoForShowingAppUpdateMessage)
  }
}
