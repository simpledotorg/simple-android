package org.simple.clinic.summary.addcholesterol

import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.util.UUID

class CholesterolEntryEffectHandlerTest {

  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val effectHandler = CholesterolEntryEffectHandler(
      clock = TestUtcClock(),
      uuidGenerator = FakeUuidGenerator(
          uuid = UUID.fromString("94c8371d-0d3a-4343-9787-da6bca1a5843"),
      ),
      medicalHistoryRepository = medicalHistoryRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
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
}
