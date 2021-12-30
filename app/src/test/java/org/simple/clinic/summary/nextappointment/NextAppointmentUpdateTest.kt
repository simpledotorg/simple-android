package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class NextAppointmentUpdateTest {

  private val updateSpec = UpdateSpec(NextAppointmentUpdate())
  private val patientUuid = UUID.fromString("18f4c3d9-0959-4008-b04a-30e360c877cd")
  private val defaultModel = NextAppointmentModel.default(patientUuid)

  @Test
  fun `when appointment is loaded, then update the model`() {
    val appointment = TestData.appointment(
        uuid = UUID.fromString("3d68e4ce-907f-432e-b73b-2c43ecb82e48")
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(AppointmentLoaded(appointment))
        .then(assertThatNext(
            hasModel(defaultModel.appointmentLoaded(appointment)),
            hasNoEffects()
        ))
  }
}
