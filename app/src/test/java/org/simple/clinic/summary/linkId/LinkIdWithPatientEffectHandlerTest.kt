package org.simple.clinic.summary.linkId

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.util.UUID

class LinkIdWithPatientEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()

  private val uiActions = mock<LinkIdWithPatientUiActions>()
  private val user = TestData.loggedInUser(uuid = UUID.fromString("5039c37f-3752-4dcb-ad69-0b6e38e02107"))
  private val identifierUuid = UUID.fromString("097a39e5-945f-44de-8293-f75960c0a54e")
  private val uuidGenerator = FakeUuidGenerator.fixed(identifierUuid)

  private val effectHandler = LinkIdWithPatientEffectHandler(
      currentUser = { user },
      patientRepository = patientRepository,
      uuidGenerator = uuidGenerator,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the get patient name from id effect is received, then the patient name must be fetched`() {
    // given
    val patientUuid = UUID.fromString("9e2bd2b6-d50c-4dfb-bd4a-c4119ac80365")
    val patientName = "TestName"
    val patient = TestData.patient(uuid = patientUuid, fullName = patientName)
    whenever(patientRepository.patientImmediate(patientUuid)).thenReturn(patient)

    // when
    testCase.dispatch(GetPatientNameFromId(patientUuid))

    //then
    testCase.assertOutgoingEvents(PatientNameReceived(patientName))
    verifyNoMoreInteractions(uiActions)
  }
}
