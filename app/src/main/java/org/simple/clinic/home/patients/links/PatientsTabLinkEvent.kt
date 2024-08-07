package org.simple.clinic.home.patients.links

import android.Manifest
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.facility.Facility
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.questionnaire.QuestionnaireResponseSections
import org.simple.clinic.questionnaire.QuestionnaireSections
import org.simple.clinic.widgets.UiEvent
import java.util.Optional

sealed class PatientsTabLinkEvent : UiEvent

data class CurrentFacilityLoaded(
    val facility: Facility
) : PatientsTabLinkEvent()

data class QuestionnairesLoaded(
    val questionnaireSections: QuestionnaireSections
) : PatientsTabLinkEvent()

data class QuestionnaireResponsesLoaded(
    val questionnaireResponseSections: QuestionnaireResponseSections
) : PatientsTabLinkEvent()

data object MonthlyScreeningReportsClicked : PatientsTabLinkEvent() {
  override val analyticsName = "Link: Monthly Screening Report clicked"
}

data object MonthlySuppliesReportsClicked : PatientsTabLinkEvent() {
  override val analyticsName = "Link: Monthly Supplies Report clicked"
}

data object MonthlyDrugStockReportsClicked : PatientsTabLinkEvent() {
  override val analyticsName = "Patient Tab Links:Monthly Drug Stock Report Clicked"
}

data class DownloadPatientLineListClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    override val permissionRequestCode: Int = 1
) : PatientsTabLinkEvent(), RequiresPermission {
  override val analyticsName = "Link: Download Patient Line List clicked"
}
