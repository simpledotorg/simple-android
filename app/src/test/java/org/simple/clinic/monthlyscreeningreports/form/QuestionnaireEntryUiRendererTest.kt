package org.simple.clinic.monthlyscreeningreports.form

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class QuestionnaireEntryUiRendererTest {

  private val ui = mock<QuestionnaireEntryUi>()
  private val uiRenderer = QuestionnaireEntryUiRenderer(ui)

  private val defaultModel = QuestionnaireEntryModel.from(questionnaireResponse = TestData.questionnaireResponse())

  @Test
  fun `when questionnaire form and response is loaded, then render questionnaire form layout`() {
    // given
    val questionnaire = TestData.questionnaire(
        uuid = UUID.fromString("43d8c0b3-3341-4147-9a08-aac27e7f721f")
    )

    val model = defaultModel
        .formLoaded(questionnaire)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).displayQuestionnaireFormLayout(questionnaire.layout)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when current facility loaded, then set facility title`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("d05d20cf-858b-432a-a312-f402681e56b3"),
        name = "PHC Simple"
    )

    val model = defaultModel
        .currentFacilityLoaded(facility)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setFacility(facility.name)
    verifyNoMoreInteractions(ui)
  }
}
