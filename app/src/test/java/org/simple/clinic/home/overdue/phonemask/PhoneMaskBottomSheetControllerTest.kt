package org.simple.clinic.home.overdue.phonemask

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.Dialer.Automatic
import org.simple.clinic.phone.Dialer.Manual
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
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
  private val clock = TestUserClock()

  private val patient = PatientMocker.patient(
      uuid = patientUuid,
      fullName = "Anish Acharya",
      gender = Male,
      age = Age(30, updatedAt = Instant.now(clock)),
      dateOfBirth = null
  )
  private val phoneNumber = PatientMocker.phoneNumber(
      uuid = UUID.fromString("fe364a55-118a-4f0f-bb64-b7b0ca2e8282"),
      patientUuid = patientUuid,
      number = "1234567890"
  )

  private lateinit var controller: PhoneMaskBottomSheetController

  @Test
  @Parameters(method = "params for types of call")
  fun `when any call button is clicked, call permission should be requested`(callTypeEvent: UiEvent) {
    sheetCreated()
    uiEvents.onNext(callTypeEvent)

    verify(screen).requestCallPermission()
    verify(screen).setupView(PatientDetails(
        phoneNumber = phoneNumber.number,
        name = patient.fullName,
        gender = patient.gender,
        age = patient.age!!.value
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
    var isCompletableSubscribed = false
    val normalCallCompletable = Completable.complete().doOnSubscribe { isCompletableSubscribed = true }
    whenever(phoneCaller.normalCall(phoneNumber.number, dialer)).doReturn(normalCallCompletable)

    sheetCreated()
    uiEvents.onNext(callTypeEvent)
    uiEvents.onNext(CallPhonePermissionChanged(permission))

    assertThat(isCompletableSubscribed).isTrue()
    verify(screen).setupView(PatientDetails(
        phoneNumber = phoneNumber.number,
        name = patient.fullName,
        gender = patient.gender,
        age = patient.age!!.value
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
    var isCompletableSubscribed = false
    val secureCallCompletable = Completable.complete().doOnSubscribe { isCompletableSubscribed = true }
    whenever(phoneCaller.secureCall(phoneNumber.number, dialer)).doReturn(secureCallCompletable)

    sheetCreated()
    uiEvents.onNext(callTypeEvent)
    uiEvents.onNext(CallPhonePermissionChanged(permission))

    assertThat(isCompletableSubscribed).isTrue()
    verify(screen).setupView(PatientDetails(
        phoneNumber = phoneNumber.number,
        name = patient.fullName,
        gender = patient.gender,
        age = patient.age!!.value
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
    // when
    sheetCreated()

    // then
    val expectedPatientDetails = PatientDetails(
        phoneNumber = phoneNumber.number,
        name = patient.fullName,
        gender = patient.gender,
        // Test UTC clock is set to EPOCH
        age = 30
    )
    verify(screen).setupView(expectedPatientDetails)
  }

  @Test
  fun `when the phone masking feature is disabled, the secure call button must not be shown`() {
    // when
    sheetCreated()

    // then
    verify(screen, never()).showSecureCallButton()
  }

  @Test
  fun `when the phone masking feature is enabled, the secure call button must be shown`() {
    // given
    val config = PhoneNumberMaskerConfig(proxyPhoneNumber = "123456", phoneMaskingFeatureEnabled = true)

    // when
    sheetCreated(config)

    // then
    verify(screen).showSecureCallButton()
  }

  @Test
  fun `when the phone masking feature is enabled but the proxy number is not set, the secure call button must not be shown`() {
    // given
    val config = PhoneNumberMaskerConfig(proxyPhoneNumber = "", phoneMaskingFeatureEnabled = true)

    // when
    sheetCreated(config)

    // then
    verify(screen, never()).showSecureCallButton()
  }

  private fun sheetCreated(
      config: PhoneNumberMaskerConfig = PhoneNumberMaskerConfig(proxyPhoneNumber = "123456", phoneMaskingFeatureEnabled = false)
  ) {
    whenever(patientRepository.patient(patientUuid)).doReturn(Observable.just(patient.toOptional()))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just(phoneNumber.toOptional()))

    controller = PhoneMaskBottomSheetController(
        phoneCaller = phoneCaller,
        patientRepository = patientRepository,
        clock = clock,
        config = Observable.just(config)
    )

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(PhoneMaskBottomSheetCreated(patientUuid))
  }
}
