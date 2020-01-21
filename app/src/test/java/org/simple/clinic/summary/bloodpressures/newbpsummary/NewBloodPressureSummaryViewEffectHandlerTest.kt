package org.simple.clinic.summary.bloodpressures.newbpsummary

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class NewBloodPressureSummaryViewEffectHandlerTest {

  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val effectHandler = NewBloodPressureSummaryViewEffectHandler(
      bloodPressureRepository = bloodPressureRepository,
      schedulersProvider = TrampolineSchedulersProvider()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)
  private val patientUuid = UUID.fromString("6b00207f-a613-4adc-9a72-dff68481a3ff")

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load blood pressures effect is received, then load blood pressures`() {
    // given
    val numberOfBpsToDisplay = 3
    val bloodPressure = PatientMocker.bp(
        UUID.fromString("51ac042d-2f70-495c-a3e3-2599d8990da2"),
        patientUuid
    )
    val bloodPressures = listOf(bloodPressure)
    whenever(bloodPressureRepository.newestMeasurementsForPatient(patientUuid = patientUuid, limit = numberOfBpsToDisplay)) doReturn Observable.just(bloodPressures)

    // when
    testCase.dispatch(LoadBloodPressures(patientUuid, numberOfBpsToDisplay))

    // then
    testCase.assertOutgoingEvents(BloodPressuresLoaded(bloodPressures))
  }
}
