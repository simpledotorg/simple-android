package org.simple.clinic.bloodsugar.entry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.OpenAs.New
import org.simple.clinic.bloodsugar.entry.OpenAs.Update
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.LocalDate
import java.util.UUID

class BloodSugarEntryUiRendererTest {

  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")
  private val bloodSugarUuid = UUID.fromString("27a522a0-4896-4875-a1cb-14baa7c2d7a9")

  private val ui = mock<BloodSugarEntryUi>()
  private val bloodSugarEntryUiRenderer = BloodSugarEntryUiRenderer(ui)

  @Test
  fun `it should render the entry title when creating a new blood sugar`() {
    // given
    val testUserClock = TestUserClock()
    val year = LocalDate.now(testUserClock).year
    val bloodSugarEntryModel = BloodSugarEntryModel.create(year, New(patientUuid, Random))

    // when
    bloodSugarEntryUiRenderer.render(bloodSugarEntryModel)

    // then
    verify(ui).hideRemoveButton()
    verify(ui).showEntryTitle(Random)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `it should render the edit title when updating a blood sugar`() {
    // given
    val testUserClock = TestUserClock()
    val year = LocalDate.now(testUserClock).year
    val bloodSugarEntryModel = BloodSugarEntryModel.create(year, Update(bloodSugarUuid, Random))

    // when
    bloodSugarEntryUiRenderer.render(bloodSugarEntryModel)

    // then
    verify(ui).showRemoveButton()
    verify(ui).showEditTitle(Random)
    verifyNoMoreInteractions(ui)
  }
}
