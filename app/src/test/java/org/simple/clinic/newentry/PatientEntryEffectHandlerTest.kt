package org.simple.clinic.newentry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class PatientEntryEffectHandlerTest {
  @Test
  fun `it hides phone length errors when hide phone validation errors is dispatched`() {
    // given
    val patientEntryUi = mock<PatientEntryUi>()
    val userSession = mock<UserSession>()
    val facilityRepository = mock<FacilityRepository>()
    val patientRepository = mock<PatientRepository>()
    whenever(facilityRepository.currentFacility(any<UserSession>())).doReturn(Observable.never())
    whenever(patientRepository.ongoingEntry()).doReturn(Single.never<OngoingNewPatientEntry>())

    val testCase = EffectHandlerTestCase(PatientEntryEffectHandler.create(
        userSession = userSession,
        facilityRepository = facilityRepository,
        patientRepository = patientRepository,
        patientRegisteredCount = mock(),
        ui = patientEntryUi,
        schedulersProvider = TrampolineSchedulersProvider()
    ))

    // when
    testCase.dispatch(HidePhoneLengthErrors)

    // then
    testCase.assertNoOutgoingEvents()
    verify(patientEntryUi).showLengthTooShortPhoneNumberError(false)
    verify(patientEntryUi).showLengthTooLongPhoneNumberError(false)
    verifyNoMoreInteractions(patientEntryUi)
  }
}
