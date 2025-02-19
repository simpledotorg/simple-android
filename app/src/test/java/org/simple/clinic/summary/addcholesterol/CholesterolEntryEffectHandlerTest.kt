package org.simple.clinic.summary.addcholesterol

import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.util.UUID

class CholesterolEntryEffectHandlerTest {

  private val uiActions = mock<CholesterolEntryUiActions>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val viewEffectHandler = CholesterolEntryViewEffectHandler(uiActions)
  private val effectHandler = CholesterolEntryEffectHandler(
      clock = TestUtcClock(),
      uuidGenerator = FakeUuidGenerator(
          uuid = UUID.fromString("94c8371d-0d3a-4343-9787-da6bca1a5843"),
      ),
      medicalHistoryRepository = medicalHistoryRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectConsumer = viewEffectHandler::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun teardown() {
    testCase.dispose()
  }

  @Test
  fun `when save cholesterol effect is received, then save cholesterol value`() {
    // given
    val patientUuid = UUID.fromString("f6d17dbb-845e-4288-8dce-003e6e4743f8")

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        defaultHistoryUuid = UUID.fromString("94c8371d-0d3a-4343-9787-da6bca1a5843"),
        patientUuid = patientUuid
    )) doReturn TestData.medicalHistory()

    val effect = SaveCholesterol(
        patientUuid = patientUuid,
        cholesterolValue = 400f
    )

    // when
    testCase.dispatch(effect)

    // then
    testCase.assertOutgoingEvents(CholesterolSaved)
  }

  @Test
  fun `when hide cholesterol error message effect is received, then hide the errors`() {
    // when
    testCase.dispatch(HideCholesterolErrorMessage)

    // then
    verify(uiActions).hideErrorMessage()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when dismiss sheet effect is received, then dismiss sheet`() {
    // when
    testCase.dispatch(DismissSheet)

    // then
    verify(uiActions).dismissSheet()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show req max cholesterol validation error effect is received, then show error`() {
    // when
    testCase.dispatch(ShowReqMaxCholesterolValidationError)

    // then
    verify(uiActions).showReqMaxCholesterolError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show req min cholesterol validation error effect is received, then show error`() {
    // when
    testCase.dispatch(ShowReqMinCholesterolValidationError)

    // then
    verify(uiActions).showReqMinCholesterolError()
    verifyNoMoreInteractions(uiActions)
  }
}
