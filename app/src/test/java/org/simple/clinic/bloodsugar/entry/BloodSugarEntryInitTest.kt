package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.PrefillDate.PrefillCurrentDate
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.LocalDate
import java.util.UUID

class BloodSugarEntryInitTest {

  @Test
  fun `when screen is created, then prefill date`() {
    val testUserClock = TestUserClock()
    val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")
    val openAs = New(patientUuid, Random)
    val model = BloodSugarEntryModel.create(LocalDate.now(testUserClock).year, openAs)
    val initSpec = InitSpec<BloodSugarEntryModel, BloodSugarEntryEffect>(BloodSugarEntryInit())

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(PrefillCurrentDate as BloodSugarEntryEffect)
        ))
  }
}
