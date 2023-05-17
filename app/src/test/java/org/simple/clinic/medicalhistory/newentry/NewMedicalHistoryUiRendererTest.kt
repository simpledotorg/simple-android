package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAHeartAttack
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAKidneyDisease
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAStroke
import org.simple.sharedTestCode.TestData
import java.util.UUID

class NewMedicalHistoryUiRendererTest {

  private val facilityWithDiabetesManagementEnabled = TestData
      .facility(
          uuid = UUID.fromString("fb08c52e-24ac-4fa8-8573-13edd2f06232"),
          facilityConfig = FacilityConfig(
              diabetesManagementEnabled = true,
              teleconsultationEnabled = false,
              monthlyScreeningReportsEnabled = false,
              monthlySuppliesReportsEnabled = false
          )
      )

  private val facilityWithDiabetesManagementDisabled = TestData
      .facility(
          uuid = UUID.fromString("66a52e56-b773-4692-b19c-a58636c6d85a"),
          facilityConfig = FacilityConfig(
              diabetesManagementEnabled = false,
              teleconsultationEnabled = false,
              monthlyScreeningReportsEnabled = false,
              monthlySuppliesReportsEnabled = false
          )
      )

  private val country = TestData.country(isoCountryCode = Country.INDIA)
  private val defaultModel = NewMedicalHistoryModel.default(country)

  private val ui = mock<NewMedicalHistoryUi>()
  private val uiRenderer = NewMedicalHistoryUiRenderer(ui)

  @Test
  fun `the medical history answers must be rendered`() {
    // given
    val model = defaultModel
        .answerChanged(DiagnosedWithHypertension, Unanswered)
        .answerChanged(HasHadAHeartAttack, Yes)
        .answerChanged(HasHadAStroke, No)
        .answerChanged(HasHadAKidneyDisease, Unanswered)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Unanswered)
    verify(ui).hideHypertensionTreatmentQuestion()
    verify(ui).renderAnswerForQuestion(HasHadAHeartAttack, Yes)
    verify(ui).renderAnswerForQuestion(HasHadAStroke, No)
    verify(ui).renderAnswerForQuestion(HasHadAKidneyDisease, Unanswered)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideDiabetesDiagnosisView()
    verify(ui).showDiabetesHistorySection()
    verify(ui).renderAnswerForQuestion(DiagnosedWithDiabetes, Unanswered)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the facility supports diabetes management, show the diagnosis view and hide the diabetes history question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, Yes)
        .answerChanged(DiagnosedWithDiabetes, No)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiabetesDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Yes)
    verify(ui).renderDiagnosisAnswer(DiagnosedWithDiabetes, No)
    verify(ui).hideNextButtonProgress()
    verify(ui).showHypertensionTreatmentQuestion(Unanswered)
    verify(ui).hideDiabetesTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the facility does not support diabetes management, hide the diabetes diagnosis view and show the diabetes history question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementDisabled)
        .answerChanged(DiagnosedWithDiabetes, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).hideDiabetesDiagnosisView()
    verify(ui).showDiabetesHistorySection()
    verify(ui).renderAnswerForQuestion(DiagnosedWithDiabetes, Yes)
    verify(ui).hideNextButtonProgress()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Unanswered)
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
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Unanswered)
    verify(ui).hideHypertensionTreatmentQuestion()
    verify(ui).showNextButtonProgress()
    verify(ui).hideDiabetesDiagnosisView()
    verify(ui).showDiabetesHistorySection()
    verify(ui).renderAnswerForQuestion(DiagnosedWithDiabetes, Unanswered)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient has hypertension and country is india, then show hypertension treatment question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiabetesDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Yes)
    verify(ui).renderDiagnosisAnswer(DiagnosedWithDiabetes, Unanswered)
    verify(ui).hideNextButtonProgress()
    verify(ui).showHypertensionTreatmentQuestion(Unanswered)
    verify(ui).hideDiabetesTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient has hypertension and country is not from india, then don't show hypertension treatment question`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = Country.BANGLADESH)
    val model = NewMedicalHistoryModel.default(country = bangladesh)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithHypertension, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiabetesDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Yes)
    verify(ui).renderDiagnosisAnswer(DiagnosedWithDiabetes, Unanswered)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verify(ui).hideDiabetesTreatmentQuestion()
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
    verify(ui).showDiabetesDiagnosisView()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Unanswered)
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithDiabetes, Unanswered)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verify(ui).hideDiabetesTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when diabetes management is enabled and patient has diabetes and is from india, then show diabetes treatment question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithDiabetes, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiabetesDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Unanswered)
    verify(ui).renderDiagnosisAnswer(DiagnosedWithDiabetes, Yes)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verify(ui).showDiabetesTreatmentQuestion(Unanswered)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when diabetes management is enabled and patient has diabetes and is not from india, then don't show diabetes treatment question`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = Country.BANGLADESH)
    val model = NewMedicalHistoryModel.default(country = bangladesh)
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithDiabetes, Yes)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiabetesDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Unanswered)
    verify(ui).renderDiagnosisAnswer(DiagnosedWithDiabetes, Yes)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verify(ui).hideDiabetesTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when diabetes management is enabled and patient does not have diabetes, then don't show diabetes treatment question`() {
    // given
    val model = defaultModel
        .currentFacilityLoaded(facilityWithDiabetesManagementEnabled)
        .answerChanged(DiagnosedWithDiabetes, No)

    // when
    uiRenderer.render(model)

    // then
    verifyImplicitRenders()
    verify(ui).showDiabetesDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verify(ui).renderDiagnosisAnswer(DiagnosedWithHypertension, Unanswered)
    verify(ui).renderDiagnosisAnswer(DiagnosedWithDiabetes, No)
    verify(ui).hideNextButtonProgress()
    verify(ui).hideHypertensionTreatmentQuestion()
    verify(ui).hideDiabetesTreatmentQuestion()
    verifyNoMoreInteractions(ui)
  }

  private fun verifyImplicitRenders() {
    verify(ui).renderAnswerForQuestion(HasHadAHeartAttack, Unanswered)
    verify(ui).renderAnswerForQuestion(HasHadAStroke, Unanswered)
    verify(ui).renderAnswerForQuestion(HasHadAKidneyDisease, Unanswered)
  }
}
