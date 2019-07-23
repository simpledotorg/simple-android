package org.simple.clinic.allpatientsinfacility

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class AllPatientsInFacilityUiControllerTest {

  @Test
  fun `verify that the state producer and ui change producer are connected`() {
    // given
    val user = PatientMocker.loggedInUser()
    val userSession = mock<UserSession>()
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))

    val facility = PatientMocker.facility(UUID.fromString("1be5097b-1c9f-4f78-aa70-9b907f241669"))
    val facilityRepository = mock<FacilityRepository>()
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    val patientRepository = mock<PatientRepository>()
    whenever(patientRepository.allPatientsInFacility(facility)).thenReturn(Observable.just(emptyList()))

    val viewStateProducer = AllPatientsInFacilityUiStateProducer(
        AllPatientsInFacilityUiState.FETCHING_PATIENTS,
        userSession,
        facilityRepository,
        patientRepository,
        TrampolineSchedulersProvider()
    )
    val uiChangeProducer = AllPatientsInFacilityUiChangeProducer()
    val controller = AllPatientsInFacilityUiController(viewStateProducer, uiChangeProducer)
    val uiEvents = PublishSubject.create<UiEvent>()
    val view = mock<AllPatientsInFacilityUi>()

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(view) }

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(view).showNoPatientsFound(facility.name)
    verifyNoMoreInteractions(view)
  }
}
