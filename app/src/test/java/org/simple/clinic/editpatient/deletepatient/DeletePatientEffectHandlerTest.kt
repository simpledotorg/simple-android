package org.simple.clinic.editpatient.deletepatient

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
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

  @Test
  fun `when show home screen effect is received, the navigate to home screen`() {
    // when
    testCase.dispatch(ShowHomeScreen)

    // then
    testCase.assertNoOutgoingEvents()

    verifyZeroInteractions(patientRepository)

    verify(uiActions).showHomeScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load patient effect is received, then load the patient`() {
    // given
    val patientUuid = UUID.fromString("d478b859-9a35-4898-9ecd-24f86d2a48dd")
    val patient = TestData.patient(
        uuid = patientUuid
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    testCase.dispatch(LoadPatient(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientLoaded(patient))

    verify(patientRepository).patientImmediate(patientUuid)
    verifyNoMoreInteractions(patientRepository)

    verifyZeroInteractions(uiActions)
  }
}
