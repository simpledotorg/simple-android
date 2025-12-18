package org.simple.clinic.summary.medicalhistory

import io.reactivex.Observable
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.simple.clinic.TestData
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.remoteconfig.DefaultValueConfigReader
import org.simple.clinic.util.NoOpRemoteConfigService
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.util.UUID

class MedicalHistorySummaryEffectHandlerTest {

  private val facility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val clock = TestUtcClock()
  private val uuidGenerator = FakeUuidGenerator(uuid = UUID.fromString("e78ec5f7-6fe9-4812-a894-9e34e55c670e"))

  private val features = Features(
      remoteConfigService = NoOpRemoteConfigService(DefaultValueConfigReader()),
      overrides = mapOf(
          Feature.Screening to true,
      )
  )
  private val effectHandler = MedicalHistorySummaryEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      medicalHistoryRepository = medicalHistoryRepository,
      clock = clock,
      currentFacility = { facility },
      uuidGenerator = uuidGenerator,
      features = features,
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @Test
  fun `when load medical history effect is received, then load medical history`() {
    // given
    val patientUuid = UUID.fromString("642ba684-25f3-4750-ba52-cd21ebf7cfca")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("8568fd00-7de8-469e-a869-e92e54eb7f9b"),
        patientUuid = patientUuid
    )

    whenever(medicalHistoryRepository.historyForPatientOrDefault(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )) doReturn Observable.just(medicalHistory)

    // when
    testCase.dispatch(LoadMedicalHistory(patientUUID = patientUuid))

    // then
    testCase.assertOutgoingEvents(MedicalHistoryLoaded(medicalHistory))
  }

  @Test
  fun `when determine suspected option visibility effect is received, then determine suspected option visibility`() {
    // given
    val medicalHistory = TestData.medicalHistory(
        uuid = uuidGenerator.v4(),
        patientUuid = uuidGenerator.v4(),
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.Unanswered,
        syncStatus = SyncStatus.DONE
    )

    // when
    testCase.dispatch(DetermineSuspectedOptionVisibility(medicalHistory = medicalHistory))

    // then
    testCase.assertOutgoingEvents(SuspectedOptionVisibilityDetermined(
        showHypertensionSuspectedOption = false,
        showDiabetesSuspectedOption = true
    ))
  }
}
