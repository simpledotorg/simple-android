package org.simple.clinic.summary.teleconsultation.status

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import java.util.UUID

class TeleconsultStatusUpdateTest {

  private val updateSpec = UpdateSpec(TeleconsultStatusUpdate())

  private val teleconsultRecordId = UUID.fromString("60af7f4a-b61f-40ae-a2b9-9f5273a70d4e")
  private val model = TeleconsultStatusModel.create(teleconsultRecordId = teleconsultRecordId)

  @Test
  fun `when teleconsult status is changed, the update the model`() {
    updateSpec
        .given(model)
        .whenEvent(TeleconsultStatusChanged(TeleconsultStatus.StillWaiting))
        .then(assertThatNext(
            hasModel(model.teleconsultStatusChanged(TeleconsultStatus.StillWaiting)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when teleconsult status is updated, then close the sheet`() {
    val teleconsultStatusModel = model.teleconsultStatusChanged(TeleconsultStatus.Yes)

    updateSpec
        .given(teleconsultStatusModel)
        .whenEvent(TeleconsultStatusUpdated)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheet)
        ))
  }
}
