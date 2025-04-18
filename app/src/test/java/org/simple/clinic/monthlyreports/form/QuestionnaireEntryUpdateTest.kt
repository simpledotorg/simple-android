package org.simple.clinic.monthlyreports.form

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class QuestionnaireEntryUpdateTest {

  private val defaultModel = QuestionnaireEntryModel.from(questionnaireResponse = TestData.questionnaireResponse())

  private val update = QuestionnaireEntryUpdate()

  private val spec = UpdateSpec(update)

  @Test
  fun `when questionnaire form is fetched, then update the model`() {
    val questionnaire = TestData.questionnaire()

    spec
        .given(defaultModel)
        .whenEvent(QuestionnaireFormFetched(questionnaire))
        .then(assertThatNext(
            hasModel(defaultModel.formLoaded(questionnaire)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when current facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("7dc68c5a-952d-46e7-83b1-070ce3d32600")
    )

    spec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.currentFacilityLoaded(facility)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when questionnaire response is saved, then go to complete screen`() {
    spec
        .given(defaultModel)
        .whenEvent(QuestionnaireResponseSaved)
        .then(assertThatNext(
            hasNoModel(),
            NextMatchers.hasEffects(GoToMonthlyReportsCompleteScreen)
        ))
  }

  @Test
  fun `when back is clicked, then show unsaved changes warning dialog`() {
    spec
        .given(defaultModel)
        .whenEvent(QuestionnaireEntryBackClicked)
        .then(assertThatNext(
            hasNoModel(),
            NextMatchers.hasEffects(ShowUnsavedChangesWarningDialog)
        ))
  }

  @Test
  fun `when submit btn is clicked, then save questionnaire response`() {
    val content = TestData.getQuestionnaireResponseContent().toMutableMap()
    val model = defaultModel.formLoaded(TestData.questionnaire())
    spec
        .given(model)
        .whenEvent(SubmitButtonClicked(content))
        .then(assertThatNext(
            hasNoModel(),
            NextMatchers.hasEffects(SaveQuestionnaireResponseEffect(model.updateContent(content).questionnaireResponse))
        ))
  }
}
