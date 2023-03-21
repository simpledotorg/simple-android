package org.simple.clinic.monthlyscreeningreports.form

import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.sharedTestCode.TestData

class QuestionnaireEntryInitTest {
  private val questionnaireType = MonthlyScreeningReports
  private val questionnaireResponse = TestData.questionnaireResponse()

  private val spec = InitSpec(QuestionnaireEntryInit(questionnaireType))

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = QuestionnaireEntryModel.from(questionnaireResponse = questionnaireResponse)

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
