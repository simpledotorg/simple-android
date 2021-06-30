package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import java.util.UUID

class NewMedicalHistoryUiRendererTest {

  private val facilityWithDiabetesManagementEnabled = TestData
      .facility(
          uuid = UUID.fromString("fb08c52e-24ac-4fa8-8573-13edd2f06232"),
          facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
      )

  private val facilityWithDiabetesManagementDisabled = TestData
      .facility(
          uuid = UUID.fromString("66a52e56-b773-4692-b19c-a58636c6d85a"),
          facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
      )

  private val country = TestData.country(isoCountryCode = Country.INDIA)
  private val defaultModel = NewMedicalHistoryModel.default(country)

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
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the facility supports diabetes management, show the diagnosis view and hide the diabetes history question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
        .answerChanged(DIAGNOSED_WITH_DIABETES, No)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, Yes)
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_DIABETES, No)
    verify(ui).hideNextButtonProgress()
    verify(ui).showHypertensionTreatmentQuestion(Unanswered)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the facility does not support diabetes management, hide the diagnosis view and show the diabetes history question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
        .answerChanged(DIAGNOSED_WITH_DIABETES, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).hideDiagnosisView()
    verify(ui).showDiabetesHistorySection()
    verify(ui).renderAnswerForQuestion(DIAGNOSED_WITH_DIABETES, Yes)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient is being saved, then show next button progress`() {
    // given
    val model = defaultModel
        .registeringPatient()

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient has hypertension and country is india, then show hypertension treatment question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, Yes)
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_DIABETES, Unanswered)
    verify(ui).hideNextButtonProgress()
    verify(ui).showHypertensionTreatmentQuestion(Unanswered)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient has hypertension and country is not from india, then show hypertension treatment question`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = Country.BANGLADESH)
    val model = NewMedicalHistoryModel.default(country = bangladesh)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, Yes)
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_DIABETES, Unanswered)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient does not have hypertension, then hide hypertension treatment question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiagnosisView()
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_HYPERTENSION, Unanswered)
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DIAGNOSED_WITH_DIABETES, Unanswered)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  private fun verifyImplicitRenders() {
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_HEART_ATTACK, Unanswered)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_STROKE, Unanswered)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_KIDNEY_DISEASE, Unanswered)
  }
}
