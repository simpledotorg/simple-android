package org.simple.clinic.monthlyReports.questionnaire.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class QuestionnaireEntryUpdate(
) : Update<QuestionnaireEntryModel, QuestionnaireEntryEvent, QuestionnaireEntryEffect> {
  override fun update(model: QuestionnaireEntryModel, event: QuestionnaireEntryEvent): Next<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
    return when (event) {
      is QuestionnaireFormFetched -> Next.next(model.formLoaded(event.questionnaire))
    }
  }
}
