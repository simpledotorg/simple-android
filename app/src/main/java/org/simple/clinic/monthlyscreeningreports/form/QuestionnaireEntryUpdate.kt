package org.simple.clinic.monthlyscreeningreports.form

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class QuestionnaireEntryUpdate :
    Update<QuestionnaireEntryModel, QuestionnaireEntryEvent, QuestionnaireEntryEffect> {
  override fun update(model: QuestionnaireEntryModel, event: QuestionnaireEntryEvent):
      Next<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
      is QuestionnaireFormFetched -> next(model.formLoaded(event.questionnaire))
      is QuestionnaireResponseSaved -> dispatch(GoToMonthlyScreeningReportsCompleteScreen)
      is QuestionnaireEntryBackClicked -> dispatch(ShowUnsavedChangesWarningDialog)
      is UnsavedChangesWarningLeavePageClicked -> dispatch(GoBack)
      is SubmitButtonClicked -> {
        val updatedModel = model.updateContent(event.content)
        dispatch(SaveQuestionnaireResponseEffect(updatedModel.questionnaireResponse))
      }
    }
  }
}
