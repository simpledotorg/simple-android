package org.simple.clinic.removeoverdueappointment

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.contactpatient.RemoveAppointmentReason

class RemoveOverdueUpdateTest {

  private val updateSpec = UpdateSpec(RemoveOverdueUpdate())
  private val defaultModel = RemoveOverdueModel.create()

  @Test
  fun `when remove appointment reason is clicked, then update the UI`() {
    val selectedReason = RemoveAppointmentReason.AlreadyVisited
    val expectedModel = defaultModel
        .removeAppointmentReasonSelected(selectedReason)

    updateSpec
        .given(defaultModel)
        .whenEvent(RemoveAppointmentReasonSelected(selectedReason))
        .then(assertThatNext(
            hasModel(expectedModel),
            hasNoEffects()
        ))
  }
}
