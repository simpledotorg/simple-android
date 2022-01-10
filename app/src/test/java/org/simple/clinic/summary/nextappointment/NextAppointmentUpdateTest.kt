package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class NextAppointmentUpdateTest {

  private val updateSpec = UpdateSpec(NextAppointmentUpdate())
  private val patientUuid = UUID.fromString("18f4c3d9-0959-4008-b04a-30e360c877cd")
  private val defaultModel = NextAppointmentModel.default(patientUuid, LocalDate.parse("2018-01-01"))

  @Test
  fun `when next appointment patient profile is loaded, then update the model`() {
    val patient = TestData.patient(
        uuid = UUID.fromString("00679b32-4097-482a-af2d-f4377ace870f"),
        fullName = "Ramesh"
    )

    val facility = TestData.facility(
        uuid = UUID.fromString("c59e4755-d32d-41c4-b69f-4c7432138b68"),
        name = "PHC Obvious"
    )

    val appointment = TestData.appointment(
        uuid = UUID.fromString("3d68e4ce-907f-432e-b73b-2c43ecb82e48"),
        patientUuid = patient.uuid,
        facilityUuid = facility.uuid
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)

    updateSpec
        .given(defaultModel)
        .whenEvent(NextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile))
        .then(assertThatNext(
            hasModel(defaultModel.nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when refresh appointment event is triggered, then load next appointment patient profile`() {
    val patient = TestData.patient(
        uuid = UUID.fromString("00679b32-4097-482a-af2d-f4377ace870f"),
        fullName = "Ramesh"
    )

    val facility = TestData.facility(
        uuid = UUID.fromString("c59e4755-d32d-41c4-b69f-4c7432138b68"),
        name = "PHC Obvious"
    )

    val appointment = TestData.appointment(
        uuid = UUID.fromString("3d68e4ce-907f-432e-b73b-2c43ecb82e48"),
        patientUuid = patient.uuid,
        facilityUuid = facility.uuid
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)
    val nextAppointmentPatientProfileLoadedModel = defaultModel
        .nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile)

    updateSpec
        .given(nextAppointmentPatientProfileLoadedModel)
        .whenEvent(RefreshAppointment)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadNextAppointmentPatientProfile(patientUuid))
        ))
  }
}
