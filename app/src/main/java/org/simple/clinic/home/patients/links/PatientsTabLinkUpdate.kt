package org.simple.clinic.home.patients.links

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class PatientsTabLinkUpdate :
    Update<PatientsTabLinkModel, PatientsTabLinkEvent, PatientsTabLinkEffect> {
  override fun update(model: PatientsTabLinkModel, event: PatientsTabLinkEvent):
      Next<PatientsTabLinkModel, PatientsTabLinkEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> Next.next(model.currentFacilityLoaded(event.facility))

      is QuestionnairesLoaded -> {
        Next.next(model.questionnairesLoaded(event.questionnaireSections))
      }

      is QuestionnaireResponsesLoaded -> {
        Next.next(model.questionnairesResponsesLoaded(event.questionnaireResponseSections))
      }

      is MonthlyScreeningReportsClicked -> dispatch(OpenMonthlyScreeningReportsListScreen)
      is MonthlySuppliesReportsClicked -> dispatch(OpenMonthlySuppliesReportsListScreen)
      is DownloadPatientLineListClicked -> dispatch(OpenPatientLineListDownloadDialog)
    }
  }
}
