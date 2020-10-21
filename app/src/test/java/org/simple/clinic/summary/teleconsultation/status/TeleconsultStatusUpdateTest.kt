package org.simple.clinic.summary.teleconsultation.status

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus

class TeleconsultStatusUpdateTest {

  @Test
  fun `when teleconsult status is changed, the update the model`() {
    val updateSpec = UpdateSpec(TeleconsultStatusUpdate())
    val model = TeleconsultStatusModel.create()

    updateSpec
        .given(model)
        .whenEvent(TeleconsultStatusChanged(TeleconsultStatus.StillWaiting))
        .then(assertThatNext(
            hasModel(model.teleconsultStatusChanged(TeleconsultStatus.StillWaiting)),
            hasNoEffects()
        ))
  }
}
