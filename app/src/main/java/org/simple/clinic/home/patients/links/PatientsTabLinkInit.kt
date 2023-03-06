package org.simple.clinic.home.patients.links

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class PatientsTabLinkInit : Init<PatientsTabLinkModel, PatientsTabLinkEffect> {

  override fun init(model: PatientsTabLinkModel): First<PatientsTabLinkModel, PatientsTabLinkEffect> {
    return first(model,
        LoadCurrentFacility,
        LoadMonthlyScreeningReportResponseList
    )
  }
}
