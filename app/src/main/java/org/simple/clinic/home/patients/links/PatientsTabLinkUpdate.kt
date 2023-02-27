package org.simple.clinic.home.patients.links

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class PatientsTabLinkUpdate :
    Update<PatientsTabLinkModel, PatientsTabLinkEvent, PatientsTabLinkEffect> {
  override fun update(model: PatientsTabLinkModel, event: PatientsTabLinkEvent):
      Next<PatientsTabLinkModel, PatientsTabLinkEffect> {
    return when (event) {
      is MonthlyScreeningReportsFormFetched -> Next.next(model.formLoaded(event.questionnaire))
      is MonthlyScreeningReportsListFetched -> Next.next(model.reportListLoaded(event.questionnaireResponseList))
      is MonthlyScreeningReportsClicked -> dispatch(OpenMonthlyScreeningReportsListScreen)
      is DownloadPatientLineListClicked -> dispatch(OpenPatientLineListDownloadDialog)
    }
  }
}
