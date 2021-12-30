package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientAndAssignedFacility
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

  @Test
  fun `when patient and assigned facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("337e7b23-cd84-4fff-8a57-4f1b376c7d69"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = UUID.fromString("5c23d24c-fbb4-49d5-bf71-877412d766fa"),
        fullName = "Ramesh Mehta",
        assignedFacilityId = UUID.fromString("337e7b23-cd84-4fff-8a57-4f1b376c7d69")
    )

    val patientAndAssignedFacility = PatientAndAssignedFacility(patient, facility)

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientAndAssignedFacilityLoaded(patientAndAssignedFacility))
        .then(assertThatNext(
            hasModel(defaultModel.patientAndAssignedFacilityLoaded(patientAndAssignedFacility)),
            hasNoEffects()
        ))
  }
}
