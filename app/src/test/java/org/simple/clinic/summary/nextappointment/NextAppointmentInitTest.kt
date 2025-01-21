package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class NextAppointmentInitTest {

  private val patientUuid = UUID.fromString("d7daec34-2a9c-44d1-9892-c38d82af1243")
  private val defaultModel = NextAppointmentModel.default(
      patientUuid = patientUuid,
      currentDate = LocalDate.parse("2018-01-01")
  )
  private val initSpec = InitSpec(NextAppointmentInit())

  @Test
  fun `when view is created, then load appointment`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadNextAppointmentPatientProfile(patientUuid = patientUuid))
        ))
  }

  @Test
  fun `when view is restored, then don't load appointment`() {
    val facilityUuid = UUID.fromString("3eaab5df-9479-48a6-acb6-3cd4b9f12326")
    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Ramesh"
    )

    val facility = TestData.facility(
        uuid = facilityUuid,
        name = "PHC Obvious"
    )

    val appointment = TestData.appointment(
        uuid = UUID.fromString("fce259b8-793a-4e92-8a27-423258be3bf7"),
        patientUuid = patientUuid
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)

    val appointmentLoadedModel = defaultModel.nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile)

    initSpec
        .whenInit(appointmentLoadedModel)
        .then(assertThatFirst(
            hasModel(appointmentLoadedModel),
            hasNoEffects()
        ))
  }
}
