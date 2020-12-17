package org.simple.clinic.bp.assignbppassport

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import dagger.Lazy
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class BpPassportEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val facility = mock<Facility>()
  private val uiActions = mock<BpPassportUiActions>()
  private val effectHandler = BpPassportEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      currentFacility = Lazy { facility },
      uiActions = uiActions
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)
  private val bpPassportNumber = 1111111

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when save new on going patient entry effect is received, then save the ongoing entry`() {
    // given
    val ongoingNewPatientEntry = TestData.ongoingPatientEntry(
        identifier = Identifier("1111111", Identifier.IdentifierType.BpPassport)
    )

    // when
    effectHandlerTestCase.dispatch(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))

    // then
    effectHandlerTestCase.assertOutgoingEvents(NewOngoingPatientEntrySaved)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when fetch current facility effect is received, then fetch facility`() {
    // when
    effectHandlerTestCase.dispatch(FetchCurrentFacility)

    // then
    effectHandlerTestCase.assertOutgoingEvents(CurrentFacilityRetrieved(facility))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open patient entry screen effect is received, then open patient entry screeen`() {
    // when
    effectHandlerTestCase.dispatch(OpenPatientEntryScreen(facility))

    // then
    verify(uiActions).openPatientEntryScreen(facility)
    effectHandlerTestCase.assertNoOutgoingEvents()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when close sheet effect is received, then close the sheet`() {
    // when
    effectHandlerTestCase.dispatch(CloseSheet)

    // then
    verify(uiActions).closeSheet()
    effectHandlerTestCase.assertNoOutgoingEvents()
    verifyNoMoreInteractions(uiActions)
  }

}
