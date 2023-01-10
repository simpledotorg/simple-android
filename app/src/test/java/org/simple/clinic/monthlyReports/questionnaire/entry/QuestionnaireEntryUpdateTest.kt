package org.simple.clinic.monthlyReports.questionnaire.entry

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.sharedTestCode.TestData

class QuestionnaireEntryUpdateTest {

  private val defaultModel = QuestionnaireEntryModel.default()

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
}
