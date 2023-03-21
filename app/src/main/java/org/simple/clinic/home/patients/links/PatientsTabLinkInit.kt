package org.simple.clinic.home.patients.links

import com.spotify.mobius.First
import com.spotify.mobius.Init

class PatientsTabLinkInit(
    private val isMonthlyScreeningReportsEnabled: Boolean
) : Init<PatientsTabLinkModel, PatientsTabLinkEffect> {

  override fun init(model: PatientsTabLinkModel): First<PatientsTabLinkModel, PatientsTabLinkEffect> {
    val effects = mutableSetOf<PatientsTabLinkEffect>()
    if (isMonthlyScreeningReportsEnabled) {
      effects.addAll(listOf(
          LoadCurrentFacility,
          LoadMonthlyScreeningReportResponseList,
          LoadMonthlyScreeningReportForm
      ))
    }
    return First.first(model, effects)
  }
}
