package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
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
        .create(patientUuid, showIsSmokingQuestion = true, showSmokelessTobaccoQuestion = true)
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

  @Test
  fun `when medical history is loaded, then update the model and determine the suspected option visibility`() {
    val patientUuid = UUID.fromString("b2593f11-158f-45da-9f61-25c9024f8be7")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("574b45e4-afe5-4303-9c98-57c297f0a072"),
        patientUuid = patientUuid,
    )
    val defaultModel = MedicalHistorySummaryModel
        .create(patientUuid, showIsSmokingQuestion = true, showSmokelessTobaccoQuestion = true)

    UpdateSpec(MedicalHistorySummaryUpdate())
        .given(defaultModel)
        .whenEvent(MedicalHistoryLoaded(medicalHistory))
        .then(assertThatNext(
            hasModel(defaultModel.medicalHistoryLoaded(medicalHistory)),
            hasEffects(DetermineSuspectedOptionVisibility(medicalHistory))
        ))
  }

  @Test
  fun `when medical history is loaded and suspected option visibility is already determined, then update the model`() {
    val patientUuid = UUID.fromString("b2593f11-158f-45da-9f61-25c9024f8be7")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("574b45e4-afe5-4303-9c98-57c297f0a072"),
        patientUuid = patientUuid,
    )
    val defaultModel = MedicalHistorySummaryModel
        .create(patientUuid, showIsSmokingQuestion = true, showSmokelessTobaccoQuestion = true)
        .diagnosisSuspectedOptionVisibilityLoaded(showHypertensionSuspectedOption = true, showDiabetesSuspectedOption = true)

    UpdateSpec(MedicalHistorySummaryUpdate())
        .given(defaultModel)
        .whenEvent(MedicalHistoryLoaded(medicalHistory))
        .then(assertThatNext(
            hasModel(defaultModel.medicalHistoryLoaded(medicalHistory)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when suspected option visibility is determined , then update the model`() {
    val patientUuid = UUID.fromString("b2593f11-158f-45da-9f61-25c9024f8be7")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("574b45e4-afe5-4303-9c98-57c297f0a072"),
        patientUuid = patientUuid,
    )
    val medicalHistoryLoadedModel = MedicalHistorySummaryModel
        .create(patientUuid, showIsSmokingQuestion = true, showSmokelessTobaccoQuestion = true)
        .medicalHistoryLoaded(medicalHistory)

    UpdateSpec(MedicalHistorySummaryUpdate())
        .given(medicalHistoryLoadedModel)
        .whenEvent(SuspectedOptionVisibilityDetermined(
            showHypertensionSuspectedOption = true,
            showDiabetesSuspectedOption = true
        ))
        .then(
            assertThatNext(
                hasModel(medicalHistoryLoadedModel.diagnosisSuspectedOptionVisibilityLoaded(
                    showHypertensionSuspectedOption = true,
                    showDiabetesSuspectedOption = true
                )),
                hasNoEffects()
            )
        )
  }
}
