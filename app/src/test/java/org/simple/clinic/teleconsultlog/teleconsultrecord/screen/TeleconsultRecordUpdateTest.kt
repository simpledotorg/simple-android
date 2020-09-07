package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class TeleconsultRecordUpdateTest {

  private val updateSpec = UpdateSpec(TeleconsultRecordUpdate())
  private val defaultModel = TeleconsultRecordModel.create()

  @Test
  fun `when back is clicked, then navigate to previous screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }

  @Test
  fun `when teleconsult record is created, then navigate to teleconsult success screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(TeleconsultRecordCreated)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToTeleconsultSuccess)
        ))
  }
}
