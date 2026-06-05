package org.simple.clinic.patientattribute.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patientattribute.BMIReading

class BMIEntryUpdateTest {

  private val defaultModel = BMIEntryModel.default(BMIReading(height = 177f, weight = 63f))

  private val update = BMIEntryUpdate()

  private val spec = UpdateSpec(update)

  @Test
  fun `when the save button is clicked, then close the sheet and return bmi reading`() {
    val model = defaultModel.weightChanged("63").heightChanged("177")
    spec
        .given(model)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheet(BMIReading(height = model.height.toFloat(), weight = model.weight.toFloat())))
        ))
  }

  @Test
  fun `when height is changed and length is not 3, then update the model`() {
    val height = "17"
    spec
        .given(defaultModel)
        .whenEvent(HeightChanged(height))
        .then(assertThatNext(
            hasModel(defaultModel.heightChanged(height)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when height is changed and length is 3, then update the model and change the focus to weight`() {
    val height = "177"
    spec
        .given(defaultModel)
        .whenEvent(HeightChanged(height))
        .then(assertThatNext(
            hasModel(defaultModel.heightChanged(height)),
            hasEffects(ChangeFocusToWeight)
        ))
  }

  @Test
  fun `when weight is changed, then update the model`() {
    val weight = "68"
    spec
        .given(defaultModel)
        .whenEvent(WeightChanged(weight))
        .then(assertThatNext(
            hasModel(defaultModel.weightChanged(weight)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when weight backspace is clicked and is not empty, then update the model`() {
    val model = defaultModel.weightChanged("68")
    spec
        .given(model)
        .whenEvent(WeightBackspaceClicked)
        .then(assertThatNext(
            hasModel(model.deleteWeightLastDigit()),
            hasNoEffects()
        ))
  }

  @Test
  fun `when weight backspace is clicked and is empty, then update the model and change the focus to height`() {
    spec
        .given(defaultModel)
        .whenEvent(WeightBackspaceClicked)
        .then(assertThatNext(
            hasModel(defaultModel.deleteWeightLastDigit()),
            hasEffects(ChangeFocusToHeight)
        ))
  }

  @Test
  fun `when back button is pressed, then close the sheet`() {
    spec
        .given(defaultModel)
        .whenEvent(BackPressed)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheet())
        ))
  }
}
