package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.TestData
import java.util.UUID

class MedicalHistorySummaryUpdateTest {

  @Test
  fun `when medical history answer is toggled, then update the model`() {
    val patientUuid = UUID.fromString("b2593f11-158f-45da-9f61-25c9024f8be7")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("574b45e4-afe5-4303-9c98-57c297f0a072"),
        patientUuid = patientUuid,
        hasHadHeartAttack = Answer.No,
        hasHadStroke = Answer.No,
        hasHadKidneyDisease = Answer.No,
        diagnosedWithHypertension = Answer.No,
        isOnHypertensionTreatment = Answer.No,
        isOnDiabetesTreatment = Answer.No,
        hasDiabetes = Answer.No,
        isSmoking = Answer.No
    )
    val medicalHistoryLoadedModel = MedicalHistorySummaryModel
        .create(patientUuid, true)
        .medicalHistoryLoaded(medicalHistory)

    val updatedMedicalHistory = medicalHistory
        .answered(DiagnosedWithHypertension, Answer.Yes)

    UpdateSpec(MedicalHistorySummaryUpdate())
        .given(medicalHistoryLoadedModel)
        .whenEvent(SummaryMedicalHistoryAnswerToggled(DiagnosedWithHypertension, Answer.Yes))
        .then(assertThatNext(
            hasModel(medicalHistoryLoadedModel.answerToggled(DiagnosedWithHypertension, Answer.Yes)),
            hasEffects(SaveUpdatedMedicalHistory(updatedMedicalHistory))
        ))
  }
}
