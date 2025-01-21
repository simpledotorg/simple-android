package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.OpenAs.New
import org.simple.clinic.bloodsugar.entry.OpenAs.Update
import org.simple.clinic.bloodsugar.entry.PrefillDate.PrefillCurrentDate
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.util.UUID

class BloodSugarEntryInitTest {

  private val testUserClock = TestUserClock()
  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")
  private val initSpec = InitSpec<BloodSugarEntryModel, BloodSugarEntryEffect>(BloodSugarEntryInit())
  private val bloodSugarMeasurementUuid = UUID.fromString("b83db9fd-43bf-4a99-82f5-1098dc00f613")
  private val openAs = Update(bloodSugarMeasurementUuid, Random)
  private val model = BloodSugarEntryModel.create(LocalDate.now(testUserClock).year, openAs)

  @Test
  fun `when screen is open to a new blood sugar, then prefill date`() {
    val openAs = New(patientUuid, Random)
    val model = BloodSugarEntryModel.create(LocalDate.now(testUserClock).year, openAs)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(PrefillCurrentDate as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when screen is open to update blood sugar, then fetch blood sugar measurement`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(FetchBloodSugarMeasurement(bloodSugarMeasurementUuid) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when the screen is created, then load the blood sugar unit preference`() {
    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadBloodSugarUnitPreference)
            )
        )
  }
}
