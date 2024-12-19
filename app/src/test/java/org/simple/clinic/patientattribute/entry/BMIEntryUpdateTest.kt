package org.simple.clinic.patientattribute.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patientattribute.BMIReading
import java.util.UUID

class BMIEntryUpdateTest {

  private val patientUuid = UUID.fromString("720f6fd4-4c4e-406a-b221-881990a962d4")

  private val defaultModel = BMIEntryModel.default(patientUuid)

  private val update = BMIEntryUpdate()

  private val spec = UpdateSpec(update)

  @Test
  fun `when the save button is clicked, then save the bmi`() {
    spec
        .given(defaultModel)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CreateNewBMIEntry(defaultModel.patientUUID, BMIReading(height = defaultModel.height, weight = defaultModel.weight)))
        ))
  }

  @Test
  fun `when bmi is saved, then close the sheet`() {
    spec
        .given(defaultModel)
        .whenEvent(BMISaved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheet)
        ))
  }
}
