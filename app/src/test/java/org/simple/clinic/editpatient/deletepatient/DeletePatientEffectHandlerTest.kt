package org.simple.clinic.editpatient.deletepatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.DeletedReason
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class DeletePatientEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val effectHandler = DeletePatientEffectHandler(
      schedulersProvider = TrampolineSchedulersProvider(),
      uiActions = uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when show confirm delete dialog effect is received, then show delete confirmation dialog`() {
    // given
    val deletedReason = DeletedReason.AccidentalRegistration
    val patientName = "John Doe"

    // when
    testCase.dispatch(ShowConfirmDeleteDialog(patientName = patientName, deletedReason = deletedReason))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showConfirmDeleteDialog(patientName, deletedReason)
    verifyNoMoreInteractions(uiActions)
  }
}
