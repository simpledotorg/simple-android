package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class NewMedicalHistoryUpdate : Update<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> {

  override fun update(model: NewMedicalHistoryModel, event: NewMedicalHistoryEvent): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return when(event) {
      is NewMedicalHistoryAnswerToggled -> next(model.answerChanged(event.question, event.answer))
      else -> noChange()
    }
  }
}
