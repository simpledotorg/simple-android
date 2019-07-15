package org.simple.clinic.home.overdue.phonemask

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.Dialer.Automatic
import org.simple.clinic.phone.Dialer.Manual
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PhoneMaskBottomSheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<PhoneMaskBottomSheet>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val phoneCaller = mock<PhoneCaller>()
  private val patientUuid: UUID = UUID.fromString("90d20a06-6e8b-4f26-b317-1ac18441765d")
  private val patientRepository: PatientRepository = mock()
  private val clock = TestUtcClock()

  private val controller = PhoneMaskBottomSheetController(
      phoneCaller = phoneCaller,
      patientRepository = patientRepository,
      clock = clock
  )

  @Before
  fun setUp() {
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  @Parameters(method = "params for types of call")
  fun `when any call button is clicked, call permission should be requested`(callTypeEvent: UiEvent) {
    val phoneNumber = "1234567890"
    val name = "Kumar Verma"
    val gender = Transgender
    val age = 32

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.just(PatientMocker.patient(
        uuid = patientUuid,
        fullName = name,
        gender = gender,
        age = Age(age, updatedAt = Instant.now(clock), computedDateOfBirth = LocalDate.now(clock)),
        dateOfBirth = null
    ).toOptional()))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(PatientMocker.phoneNumber(
        uuid = patientUuid,
        number = phoneNumber
    ).toOptional()))

    uiEvents.onNext(PhoneMaskBottomSheetCreated(patientUuid))
    uiEvents.onNext(callTypeEvent)

    verify(screen).requestCallPermission()
    verify(screen).setupView(PatientDetails(
        phoneNumber = phoneNumber,
        name = name,
        genderLetterRes = gender.displayLetterRes,
        age = age
    ))
    verifyNoMoreInteractions(screen)
  }

  @Suppress("Unused")
  private fun `params for types of call`() =
      listOf(NormalCallClicked, SecureCallClicked)

  @Test
  @Parameters(method = "params for making normal phone calls")
  fun `when normal call button is clicked and permission result is received, appropriate call should be made`(
      callTypeEvent: UiEvent,
      permission: RuntimePermissionResult,
      dialer: Dialer
  ) {
    val number = "1234567890"
    val name = "Kumar Verma"
    val gender = Transgender
    val age = 32

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.just(PatientMocker.patient(
        uuid = patientUuid,
        fullName = name,
        gender = gender,
        age = Age(age, updatedAt = Instant.now(clock), computedDateOfBirth = LocalDate.now(clock)),
        dateOfBirth = null
    ).toOptional()))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(PatientMocker.phoneNumber(
        uuid = patientUuid,
        number = number
    ).toOptional()))

    var isCompletableSubscribed = false
    val normalCallCompletable = Completable.complete().doOnSubscribe { isCompletableSubscribed = true }
    whenever(phoneCaller.normalCall(number, dialer)).thenReturn(normalCallCompletable)

    uiEvents.onNext(PhoneMaskBottomSheetCreated(patientUuid))
    uiEvents.onNext(callTypeEvent)
    uiEvents.onNext(CallPhonePermissionChanged(permission))

    assertThat(isCompletableSubscribed).isTrue()
    verify(screen).setupView(PatientDetails(
        phoneNumber = number,
        name = name,
        genderLetterRes = gender.displayLetterRes,
        age = age
    ))
    verify(screen).requestCallPermission()
    verify(screen).closeSheet()
    verifyNoMoreInteractions(screen)
  }

  @Test
  @Parameters(method = "params for making secure phone calls")
  fun `when secure call button is clicked and permission result is received, appropriate call should be made`(
      callTypeEvent: UiEvent,
      permission: RuntimePermissionResult,
      dialer: Dialer
  ) {
    val number = "1234567890"
    val name = "Kumar Verma"
    val gender = Transgender
    val age = 32

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.just(PatientMocker.patient(
        uuid = patientUuid,
        fullName = name,
        gender = gender,
        age = Age(age, updatedAt = Instant.now(clock), computedDateOfBirth = LocalDate.now(clock)),
        dateOfBirth = null
    ).toOptional()))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(PatientMocker.phoneNumber(
        uuid = patientUuid,
        number = number
    ).toOptional()))

    var isCompletableSubscribed = false
    val secureCallCompletable = Completable.complete().doOnSubscribe { isCompletableSubscribed = true }
    whenever(phoneCaller.secureCall(number, dialer)).thenReturn(secureCallCompletable)

    uiEvents.onNext(PhoneMaskBottomSheetCreated(patientUuid))
    uiEvents.onNext(callTypeEvent)
    uiEvents.onNext(CallPhonePermissionChanged(permission))

    assertThat(isCompletableSubscribed).isTrue()
    verify(screen).setupView(PatientDetails(
        phoneNumber = number,
        name = name,
        genderLetterRes = gender.displayLetterRes,
        age = age
    ))
    verify(screen).requestCallPermission()
    verify(screen).closeSheet()
    verifyNoMoreInteractions(screen)
  }

  @Suppress("Unused")
  private fun `params for making normal phone calls`() =
      listOf(
          listOf(NormalCallClicked, GRANTED, Automatic),
          listOf(NormalCallClicked, DENIED, Manual),
          listOf(NormalCallClicked, NEVER_ASK_AGAIN, Manual)
      )

  @Suppress("Unused")
  private fun `params for making secure phone calls`() =
      listOf(
          listOf(SecureCallClicked, GRANTED, Automatic),
          listOf(SecureCallClicked, DENIED, Manual),
          listOf(SecureCallClicked, NEVER_ASK_AGAIN, Manual)
      )

  @Test
  fun `when the screen is created, the patient details must be populated`() {
    // given
    val patientSubject = PublishSubject.create<Optional<Patient>>()
    val patientPhoneNumberSubject = PublishSubject.create<Optional<PatientPhoneNumber>>()
    whenever(patientRepository.patient(patientUuid)).thenReturn(patientSubject)
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(patientPhoneNumberSubject)

    val patientName = "Anish Acharya"
    val gender = Male
    val number = "1234567890"

    val patient = PatientMocker.patient(
        uuid = patientUuid,
        fullName = patientName,
        gender = gender,
        dateOfBirth = LocalDate.of(1940, Month.JANUARY, 1),
        age = null
    )
    val phoneNumber = PatientMocker.phoneNumber(
        uuid = UUID.fromString("fe364a55-118a-4f0f-bb64-b7b0ca2e8282"),
        patientUuid = patientUuid,
        number = number
    )

    // when
    uiEvents.onNext(PhoneMaskBottomSheetCreated(patientUuid))
    patientSubject.onNext(patient.toOptional())
    patientPhoneNumberSubject.onNext(phoneNumber.toOptional())

    // then
    val expectedPatientDetails = PatientDetails(
        phoneNumber = number,
        name = patientName,
        genderLetterRes = gender.displayLetterRes,
        // Test UTC clock is set to EPOCH
        age = 30
    )
    verify(screen).setupView(expectedPatientDetails)
  }
}
