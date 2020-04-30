package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class MedicalHistorySummaryUpdate : Update<MedicalHistorySummaryModel, MedicalHistorySummaryEvent, MedicalHistorySummaryEffect> {

  private val diagnosisQuestions = setOf(DIAGNOSED_WITH_HYPERTENSION, DIAGNOSED_WITH_DIABETES)

  override fun update(
      model: MedicalHistorySummaryModel,
      event: MedicalHistorySummaryEvent
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    return when (event) {
      is MedicalHistoryLoaded -> next(model.medicalHistoryLoaded(event.medicalHistory))
      is SummaryMedicalHistoryAnswerToggled -> medicalHistoryAnswerToggled(event)
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
    }
  }

  private fun medicalHistoryAnswerToggled(
      event: SummaryMedicalHistoryAnswerToggled
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    val toggledQuestion = event.question

    return if (toggledQuestion in diagnosisQuestions) {
      dispatch(HideDiagnosisError as MedicalHistorySummaryEffect)
    } else {
      noChange()
    }
  }
}
