package org.simple.clinic.teleconsultlog.prescription.patientinfo

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class TeleconsultPatientInfoInitTest {

  private val patientUuid = UUID.fromString("fc84604e-b12c-4739-85d5-b69da85f6dc5")
  private val model = TeleconsultPatientInfoModel.create(
      patientUuid = patientUuid,
      prescriptionDate = LocalDate.parse("2018-01-01")
  )

  private val initSpec = InitSpec(TeleconsultPatientInfoInit())

  @Test
  fun `when screen is created, then load the initial data`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadPatientProfile(patientUuid))
        ))
  }

  @Test
  fun `when screen is restored, then don't load initial data`() {
    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)

    val model = model
        .patientProfileLoaded(patientProfile)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }
}
