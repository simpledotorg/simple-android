package org.simple.clinic.newentry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.newentry.Field.PhoneNumber
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class PatientEntryEffectHandlerTest {

  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val validationActions = mock<PatientEntryValidationActions>()

  private val facility = TestData.facility(uuid = UUID.fromString("e135085f-b5a1-49d4-bd77-73ad98500b92"))
  private val entry = TestData.ongoingPatientEntry()

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider(
      dateTimeFormatter = dateTimeFormatter,
      today = LocalDate.now(clock)
  ))

  private val ui = mock<PatientEntryUi>()
  private val effectHandler = PatientEntryEffectHandler(
      facilityRepository = facilityRepository,
      patientRepository = patientRepository,
      schedulersProvider = TrampolineSchedulersProvider(),
      patientRegisteredCount = mock(),
      inputFieldsFactory = inputFieldsFactory,
      ui = ui,
      validationActions = validationActions
  )

  private lateinit var testCase: EffectHandlerTestCase<PatientEntryEffect, PatientEntryEvent>

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `it hides phone length errors when hide phone validation errors is dispatched`() {
    // when
    setupTestCase()
    testCase.dispatch(HideValidationError(PhoneNumber))

    // then
    testCase.assertNoOutgoingEvents()
    verify(validationActions).showLengthTooShortPhoneNumberError(false, 0)
    verify(validationActions).showLengthTooLongPhoneNumberError(false, 0)
    verifyNoMoreInteractions(validationActions)
  }

  @Test
  fun `when the load input fields effect is received, the input fields must be loaded`() {
    // when
    setupTestCase()
    testCase.dispatch(LoadInputFields)

    // then
    val expectedFields = InputFields(inputFieldsFactory.provideFields())
    testCase.assertOutgoingEvents(InputFieldsLoaded(expectedFields))
  }

  @Test
  fun `when the setup UI effect is received, the UI must be setup with the input fields`() {
    // given
    val inputFields = InputFields(inputFieldsFactory.provideFields())

    // when
    setupTestCase()
    testCase.dispatch(SetupUi(inputFields))

    // then
    testCase.assertNoOutgoingEvents()
    verify(ui).setupUi(inputFields)
    verifyNoMoreInteractions(ui)
    verifyZeroInteractions(validationActions)
  }

  private fun setupTestCase() {
    whenever(facilityRepository.currentFacility()).thenReturn(Observable.just(facility))
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(entry))

    testCase = EffectHandlerTestCase(effectHandler.build())
  }
}
