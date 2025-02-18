package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
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
}
