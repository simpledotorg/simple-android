package org.simple.clinic.home.patients

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class PatientsInit : Init<PatientsTabModel, PatientsTabEffect> {

  override fun init(model: PatientsTabModel): First<PatientsTabModel, PatientsTabEffect> {
    return first(model, LoadUser, RefreshUserDetails, LoadNumberOfPatientsRegistered, LoadInfoForShowingAppUpdateMessage)
  }
}
