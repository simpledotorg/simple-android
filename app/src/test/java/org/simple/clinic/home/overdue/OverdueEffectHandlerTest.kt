package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class OverdueEffectHandlerTest {

  private val facility = TestData.facility(
      uuid = UUID.fromString("251deca2-d219-4863-80fc-e7d48cb22b1b"),
      name = "PHC Obvious"
  )
  private val uiActions = mock<OverdueUiActions>()
  private val effectHandler = OverdueEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      appointmentRepository = mock(),
      currentFacility = { facility },
      dataSourceFactory = mock(),
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when open patient summary effect is received, then open patient summary`() {
    // given
    val patientUuid = UUID.fromString("e6794bf5-447e-4588-8df2-5e2a07d23bc4")

    // when
    effectHandlerTestCase.dispatch(OpenPatientSummary(patientUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSummary(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the load current facility effect is received, then the current facility must be loaded`() {
    // when
    effectHandlerTestCase.dispatch(LoadCurrentFacility)

    // then
    effectHandlerTestCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show overdue appointments effect is received, then show overdue appointments`() {
    // given
    val overdueAppointments = listOf(
        TestData.overdueAppointment(
            appointmentUuid = UUID.fromString("e960f0dd-e575-4a1d-b8c1-6676097b4b54")
        ),
        TestData.overdueAppointment(
            appointmentUuid = UUID.fromString("65c380ad-d2e4-49f5-a348-07e8d489dab1")
        )
    )
    val pagingData = PagingData.from(overdueAppointments)

    // when
    effectHandlerTestCase.dispatch(ShowOverdueAppointments(overdueAppointments = pagingData,
        isDiabetesManagementEnabled = true))

    // then
    verify(uiActions).showOverdueAppointments(overdueAppointments = pagingData,
        isDiabetesManagementEnabled = true)
  }
}
