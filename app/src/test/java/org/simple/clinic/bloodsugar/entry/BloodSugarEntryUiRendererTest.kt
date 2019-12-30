package org.simple.clinic.bloodsugar.entry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.LocalDate
import java.util.UUID

class BloodSugarEntryUiRendererTest {

  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")

  private val ui = mock<BloodSugarEntryUi>()
  private val bloodSugarEntryUiRenderer = BloodSugarEntryUiRenderer(ui)

  @Test
  fun `it should render the title when creating new random blood sugar`() {
    // given
    val testUserClock = TestUserClock()
    val year = LocalDate.now(testUserClock).year
    val bloodSugarEntryModel = BloodSugarEntryModel.create(year, New(patientUuid, Random))

    // when
    bloodSugarEntryUiRenderer.render(bloodSugarEntryModel)

    // then
    verify(ui).showRandomBloodSugarTitle()
  }

  @Test
  fun `it should render the title when creating new post prandial blood sugar`() {
    // given
    val testUserClock = TestUserClock()
    val year = LocalDate.now(testUserClock).year
    val bloodSugarEntryModel = BloodSugarEntryModel.create(year, New(patientUuid, PostPrandial))

    // when
    bloodSugarEntryUiRenderer.render(bloodSugarEntryModel)

    // then
    verify(ui).showPostPrandialBloodSugarTitle()
  }

  @Test
  fun `it should render the title when creating new fasting blood sugar`() {
    // given
    val testUserClock = TestUserClock()
    val year = LocalDate.now(testUserClock).year
    val bloodSugarEntryModel = BloodSugarEntryModel.create(year, New(patientUuid, Fasting))

    // when
    bloodSugarEntryUiRenderer.render(bloodSugarEntryModel)

    // then
    verify(ui).showFastingBloodSugarTitle()
  }
}
