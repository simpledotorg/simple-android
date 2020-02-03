package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class NewMedicalHistoryEffectHandlerTest {

  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val uiActions = mock<NewMedicalHistoryUiActions>()

  private val effectHandler = NewMedicalHistoryEffectHandler(
      schedulersProvider = TrampolineSchedulersProvider(),
      uiActions = uiActions,
      userSession = userSession,
      facilityRepository = facilityRepository,
      patientRepository = mock(),
      medicalHistoryRepository = mock()
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the load current facility effect is received, the current facility should be loaded`() {
    // given
    val user = PatientMocker.loggedInUser(uuid = UUID.fromString("c70eb25b-c665-4f9d-a889-bf5504ec8af0"))
    val facility = PatientMocker.facility(uuid = UUID.fromString("5b9629f3-042b-4b0a-8bd6-f7658130eee7"))

    whenever(userSession.loggedInUserImmediate()) doReturn user
    whenever(facilityRepository.currentFacility(user)) doReturn Observable.just(facility)

    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }
}
