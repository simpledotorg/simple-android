package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.Questionnaire
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.UiEvent

sealed class QuestionnaireEntryEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : QuestionnaireEntryEvent()

data class QuestionnaireFormFetched(val questionnaire: Questionnaire) : QuestionnaireEntryEvent()

data class QuestionnaireResponseFetched(val questionnaireResponse: QuestionnaireResponse) : QuestionnaireEntryEvent()

object QuestionnaireResponseSaved : QuestionnaireEntryEvent()

object QuestionnaireEntryBackClicked : QuestionnaireEntryEvent() {
  override val analyticsName = "Monthly Screening Report Form:Back Clicked"
}

object UnsavedChangesWarningLeavePageClicked : QuestionnaireEntryEvent() {
  override val analyticsName = "Monthly Screening Report Form:Unsaved Changes Warning Leave Page Clicked"
}

data class SubmitButtonClicked(val questionnaireResponse: QuestionnaireResponse) : QuestionnaireEntryEvent()

