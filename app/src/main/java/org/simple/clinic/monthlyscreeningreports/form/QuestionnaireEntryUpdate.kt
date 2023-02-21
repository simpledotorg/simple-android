package org.simple.clinic.monthlyscreeningreports.form

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class QuestionnaireEntryUpdate :
    Update<QuestionnaireEntryModel, QuestionnaireEntryEvent, QuestionnaireEntryEffect> {
  override fun update(model: QuestionnaireEntryModel, event: QuestionnaireEntryEvent):
      Next<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> Next.next(model.currentFacilityLoaded(event.facility))
      is QuestionnaireFormFetched -> Next.next(model.formLoaded(event.questionnaire))
      is QuestionnaireEntryBackClicked -> dispatch(ShowUnsavedChangesWarningDialog)
      is UnsavedChangesWarningLeavePageClicked -> dispatch(GoBack)
    }
  }
}
