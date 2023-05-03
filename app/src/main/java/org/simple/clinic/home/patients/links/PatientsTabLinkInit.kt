package org.simple.clinic.home.patients.links

import com.spotify.mobius.First
import com.spotify.mobius.Init

class PatientsTabLinkInit : Init<PatientsTabLinkModel, PatientsTabLinkEffect> {

  override fun init(model: PatientsTabLinkModel): First<PatientsTabLinkModel, PatientsTabLinkEffect> {
    return First.first(model, mutableSetOf(
        LoadCurrentFacility,
        LoadMonthlyScreeningReportResponseList,
        LoadMonthlyScreeningReportForm
    ))
  }
}
