package org.simple.clinic.newentry

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.newentry.Field.PhoneNumber
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class PatientEntryEffectHandlerTest {
  @Test
  fun `it hides phone length errors when hide phone validation errors is dispatched`() {
    // given
    val userSession = mock<UserSession>()
    val facilityRepository = mock<FacilityRepository>()
    val patientRepository = mock<PatientRepository>()
    val validationActions = mock<PatientEntryValidationActions>()
    whenever(facilityRepository.currentFacility(any<UserSession>())).doReturn(Observable.never())
    whenever(patientRepository.ongoingEntry()).doReturn(Single.never<OngoingNewPatientEntry>())

    val testCase = EffectHandlerTestCase(PatientEntryEffectHandler(
        userSession = userSession,
        facilityRepository = facilityRepository,
        patientRepository = patientRepository,
        patientRegisteredCount = mock(),
        ui = mock(),
        validationActions = validationActions,
        schedulersProvider = TrampolineSchedulersProvider()
    ).build())

    // when
    testCase.dispatch(HideValidationError(PhoneNumber))

    // then
    testCase.assertNoOutgoingEvents()
    verify(validationActions).showLengthTooShortPhoneNumberError(false, 0)
    verify(validationActions).showLengthTooLongPhoneNumberError(false,0)
    verifyNoMoreInteractions(validationActions)
  }
}
