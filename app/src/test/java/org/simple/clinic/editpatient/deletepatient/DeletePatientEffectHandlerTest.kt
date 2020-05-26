package org.simple.clinic.editpatient.deletepatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.DeletedReason
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class DeletePatientEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<UiActions>()
  private val effectHandler = DeletePatientEffectHandler(
      patientRepository = patientRepository,
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

  @Test
  fun `when show confirm died dialog effect is received, then show confirm died dialog`() {
    // given
    val patientName = "John Doe"

    // when
    testCase.dispatch(ShowConfirmDiedDialog(patientName = patientName))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showConfirmDiedDialog(patientName)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when delete patient effect is received, then delete the patient`() {
    // given
    val patientUuid = UUID.fromString("ea31ea27-facc-49dc-a9af-5c5bcb7339de")
    val deletedReason = DeletedReason.AccidentalRegistration

    // when
    testCase.dispatch(DeletePatient(
        patientUuid = patientUuid,
        deletedReason = deletedReason
    ))

    // then
    testCase.assertOutgoingEvents(PatientDeleted)

    verify(patientRepository).deletePatient(
        patientUuid = patientUuid,
        deletedReason = deletedReason
    )
    verifyNoMoreInteractions(patientRepository)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when patient died effect is received, then mark patient status as died`() {
    // given
    val patientUuid = UUID.fromString("3dc6d1bc-cb47-44ed-b05d-ff64d92a9897")

    // when
    testCase.dispatch(MarkPatientAsDead(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientMarkedAsDead)

    verify(patientRepository).updatePatientStatusToDead(patientUuid)
    verifyNoMoreInteractions(patientRepository)

    verifyZeroInteractions(uiActions)
  }
}
