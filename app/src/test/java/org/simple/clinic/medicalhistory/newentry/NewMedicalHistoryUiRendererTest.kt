package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class NewMedicalHistoryUiRendererTest {

  private val facilityWithDiabetesManagementEnabled = PatientMocker
      .facility(
          uuid = UUID.fromString("fb08c52e-24ac-4fa8-8573-13edd2f06232"),
          facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
      )

  private val facilityWithDiabetesManagementDisabled = PatientMocker
      .facility(
          uuid = UUID.fromString("66a52e56-b773-4692-b19c-a58636c6d85a"),
          facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
      )

  private val defaultModel = NewMedicalHistoryModel.default()

  private val ui = mock<NewMedicalHistoryUi>()
  private val uiRenderer = NewMedicalHistoryUiRenderer(ui)

  @Test
  fun `the medical history answers must be rendered`() {
    // given
    val model = defaultModel
        .answerChanged(HAS_HAD_A_HEART_ATTACK, Yes)
        .answerChanged(HAS_HAD_A_STROKE, No)
        .answerChanged(HAS_HAD_A_KIDNEY_DISEASE, Unanswered)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_HEART_ATTACK, Yes)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_STROKE, No)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_KIDNEY_DISEASE, Unanswered)
    verify(ui).showDiagnosisRequiredError(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the facility supports diabetes management, show the diagnosis view and hide the diabetes history question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
        .answerChanged(HAS_DIABETES, No)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, Yes)
    verify(ui).renderDiagnosisAnswer(HAS_DIABETES, No)
    verify(ui).showDiagnosisRequiredError(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the facility does not support diabetes management, hide the diagnosis view and show the diabetes history question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
        .answerChanged(HAS_DIABETES, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).renderAnswerForQuestion(HAS_DIABETES, Yes)
    verify(ui).showDiagnosisRequiredError(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the facility supports diabetes management and the user has not selected a diagnosis, show the error`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .diagnosisRequired()

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, Unanswered)
    verify(ui).renderDiagnosisAnswer(HAS_DIABETES, Unanswered)
    verify(ui).showDiagnosisRequiredError(true)
    verifyNoMoreInteractions(ui)
  }

  private fun verifyImplicitRenders() {
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_HEART_ATTACK, Unanswered)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_STROKE, Unanswered)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_KIDNEY_DISEASE, Unanswered)
  }
}
