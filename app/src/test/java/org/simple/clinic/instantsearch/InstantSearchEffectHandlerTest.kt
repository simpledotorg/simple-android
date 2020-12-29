package org.simple.clinic.instantsearch

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class InstantSearchEffectHandlerTest {

  private val facility = TestData.facility()
  private val patientRepository = mock<PatientRepository>()
  private val effectHandler = InstantSearchEffectHandler(
      currentFacility = { facility },
      patientRepository = patientRepository,
      schedulers = TestSchedulersProvider.trampoline()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load current facility effect is received, then load the current facility`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
  }

  @Test
  fun `when load all patients effect is received, then load all patients`() {
    // given
    val patients = listOf(
        TestData.patientSearchResult(uuid = UUID.fromString("ba579c2a-e067-4ded-ab4e-86589414c6d0")),
        TestData.patientSearchResult(uuid = UUID.fromString("24be0305-04a3-4111-94e2-e0a254e38a04"))
    )

    whenever(patientRepository.allPatientsInFacility(facility)) doReturn patients

    // when
    testCase.dispatch(LoadAllPatients(facility))

    // then
    testCase.assertOutgoingEvents(AllPatientsLoaded(patients))
  }
}
