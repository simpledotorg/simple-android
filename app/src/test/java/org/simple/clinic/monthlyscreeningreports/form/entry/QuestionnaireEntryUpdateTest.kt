package org.simple.clinic.monthlyscreeningreports.form.entry

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.monthlyscreeningreports.form.CurrentFacilityLoaded
import org.simple.clinic.monthlyscreeningreports.form.QuestionnaireEntryModel
import org.simple.clinic.monthlyscreeningreports.form.QuestionnaireEntryUpdate
import org.simple.clinic.monthlyscreeningreports.form.QuestionnaireFormFetched
import org.simple.sharedTestCode.TestData
import java.util.UUID

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
}
