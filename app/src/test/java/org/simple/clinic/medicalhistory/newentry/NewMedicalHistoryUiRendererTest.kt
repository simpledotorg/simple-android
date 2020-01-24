package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION

class NewMedicalHistoryUiRendererTest {

  @Test
  fun `the medical history answers must be rendered`() {
    // given
    val ui = mock<NewMedicalHistoryUi>()
    val uiRenderer = NewMedicalHistoryUiRenderer(ui)
    val model = NewMedicalHistoryModel.default()
        .answerChanged(HAS_HAD_A_HEART_ATTACK, Yes)
        .answerChanged(HAS_HAD_A_STROKE, No)
        .answerChanged(HAS_HAD_A_KIDNEY_DISEASE, Unanswered)
        .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
        .answerChanged(IS_ON_TREATMENT_FOR_HYPERTENSION, No)
        .answerChanged(HAS_DIABETES, Unanswered)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_HEART_ATTACK, Yes)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_STROKE, No)
    verify(ui).renderAnswerForQuestion(HAS_HAD_A_KIDNEY_DISEASE, Unanswered)
    verify(ui).renderAnswerForQuestion(DIAGNOSED_WITH_HYPERTENSION, Yes)
    verify(ui).renderAnswerForQuestion(IS_ON_TREATMENT_FOR_HYPERTENSION, No)
    verify(ui).renderAnswerForQuestion(HAS_DIABETES, Unanswered)
    verifyNoMoreInteractions(ui)
  }
}
