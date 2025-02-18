package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class CholesterolEntryUpdateTest {

  private val defaultModel = CholesterolEntryModel.create()
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
}
