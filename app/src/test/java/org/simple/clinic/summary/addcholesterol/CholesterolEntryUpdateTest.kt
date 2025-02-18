package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import java.util.UUID

class CholesterolEntryUpdateTest {

  private val patientUuid = UUID.fromString("74cf8bf8-2a82-4cd0-a685-d1c3d10c42d9")
  private val defaultModel = CholesterolEntryModel.create(
      patientUUID = patientUuid
  )
  private val updateSpec = UpdateSpec(
      CholesterolEntryUpdate()
  )

  @Test
  fun `when cholesterol value changes, hide any error message`() {
    val cholesterolValue = 20f
    updateSpec
        .given(defaultModel)
        .whenEvent(CholesterolChanged(cholesterolValue))
        .then(assertThatNext(
            hasModel(defaultModel.cholesterolChanged(cholesterolValue)),
            hasEffects(HideCholesterolErrorMessage)
        ))
  }

  @Test
  fun `when cholesterol value is under min range and save is clicked then show validation error`() {
    val cholesterolValue = 20f
    updateSpec
        .given(defaultModel.cholesterolChanged(cholesterolValue))
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowReqMinCholesterolValidationError)
        ))
  }

  @Test
  fun `when cholesterol value is over max range and save is clicked then show validation error`() {
    val cholesterolValue = 1001f
    updateSpec
        .given(defaultModel.cholesterolChanged(cholesterolValue))
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowReqMaxCholesterolValidationError)
        ))
  }

  @Test
  fun `when cholesterol value is within range and save is clicked, then save the cholesterol value`() {
    val cholesterolValue = 400f
    updateSpec
        .given(defaultModel.cholesterolChanged(cholesterolValue))
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasModel(
                defaultModel
                    .cholesterolChanged(cholesterolValue)
                    .savingCholesterol()
            ),
            hasEffects(SaveCholesterol(patientUuid, cholesterolValue))
        ))
  }
}
