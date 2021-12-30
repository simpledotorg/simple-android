package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientAndAssignedFacility
import java.util.UUID

class NextAppointmentInitTest {

  private val patientUuid = UUID.fromString("d7daec34-2a9c-44d1-9892-c38d82af1243")
  private val defaultModel = NextAppointmentModel.default(
      patientUuid = patientUuid
  )
  private val initSpec = InitSpec(NextAppointmentInit())

  @Test
  fun `when view is created, then load appointment, patient and assigned facility`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadAppointment(patientUuid = patientUuid), LoadPatientAndAssignedFacility(patientUuid = patientUuid))
        ))
  }

  @Test
  fun `when view is restored, then don't load appointment, patient and assigned facility`() {
    val appointment = TestData.appointment(
        uuid = UUID.fromString("fce259b8-793a-4e92-8a27-423258be3bf7"),
        patientUuid = patientUuid
    )

    val assignedFacility = TestData.facility(
        uuid = UUID.fromString("9aa8df4e-2768-4daa-8d7b-9557dea182fa"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Ramesh Mehta",
        assignedFacilityId = assignedFacility.uuid
    )

    val patientAndAssignedFacility = PatientAndAssignedFacility(patient, assignedFacility)

    val appointmentAndPatientLoadedModel = defaultModel
        .appointmentLoaded(appointment)
        .patientAndAssignedFacilityLoaded(patientAndAssignedFacility)

    initSpec
        .whenInit(appointmentAndPatientLoadedModel)
        .then(assertThatFirst(
            hasModel(appointmentAndPatientLoadedModel),
            hasNoEffects()
        ))
  }
}
