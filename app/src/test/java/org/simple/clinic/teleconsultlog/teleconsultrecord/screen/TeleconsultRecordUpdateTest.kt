package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class TeleconsultRecordUpdateTest {

  @Test
  fun `when back is clicked, then navigate to previous screen`() {
    UpdateSpec(TeleconsultRecordUpdate())
        .given(TeleconsultRecordModel.create())
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }
}
