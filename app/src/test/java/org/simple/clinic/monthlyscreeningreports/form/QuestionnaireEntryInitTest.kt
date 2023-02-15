package org.simple.clinic.monthlyscreeningreports.form

import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.monthlyscreeningreports.form.LoadCurrentFacility
import org.simple.clinic.monthlyscreeningreports.form.LoadQuestionnaireFormEffect
import org.simple.clinic.monthlyscreeningreports.form.QuestionnaireEntryInit
import org.simple.clinic.monthlyscreeningreports.form.QuestionnaireEntryModel
import org.simple.clinic.questionnaire.MonthlyScreeningReports

class QuestionnaireEntryInitTest {
  private val questionnaireType = MonthlyScreeningReports
  private val spec = InitSpec(QuestionnaireEntryInit(questionnaireType))

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = QuestionnaireEntryModel.default()

    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(
                LoadCurrentFacility,
                LoadQuestionnaireFormEffect(questionnaireType),
            )
        ))
  }
}
