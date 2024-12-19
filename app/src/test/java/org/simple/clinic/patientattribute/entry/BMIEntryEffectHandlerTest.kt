package org.simple.clinic.patientattribute.entry

import org.junit.After
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class BMIEntryEffectHandlerTest {
  private val ui = mock<BMIEntryUi>()

  private val facility = TestData.facility(uuid = UUID.fromString("7a7df523-8397-4c9e-bcef-d0047ea2e969"))
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("5f8c9705-6732-4d1c-aea3-3b5ab10d4a0e"),
      registrationFacilityUuid = facility.uuid,
      currentFacilityUuid = facility.uuid
  )


  private val effectHandler = BMIEntryEffectHandler(
      ui = ui,
      patientAttributeRepository = mock(),
      currentUser = { user },
      uuidGenerator = org.mockito.kotlin.mock(),
      schedulersProvider = TestSchedulersProvider.trampoline(),
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun teardown() {
    testCase.dispose()
  }

  @Test
  fun `when create bmi entry is received, then bmi should be saved`() {
    //when
    testCase.dispatch(CreateNewBMIEntry(
        reading = BMIReading(height = "177", weight = "63"),
        patientUUID = UUID.randomUUID()
    ))

    //then
    testCase.assertOutgoingEvents(BMISaved)
  }

  @Test
  fun `when close sheet view effect is received, then close sheet`() {
    //when
    testCase.dispatch(CloseSheet)

    //then
    testCase.assertNoOutgoingEvents()
    verify(ui).closeSheet()
  }

  @Test
  fun `when change focus to height view effect is received, then change the focus to height`() {
    //when
    testCase.dispatch(ChangeFocusToHeight)

    //then
    testCase.assertNoOutgoingEvents()
    verify(ui).changeFocusToHeight()
  }

  @Test
  fun `when change focus to weight view effect is received, then change the focus to weight`() {
    //when
    testCase.dispatch(ChangeFocusToWeight)

    //then
    testCase.assertNoOutgoingEvents()
    verify(ui).changeFocusToWeight()
  }
}
