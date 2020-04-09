package org.simple.clinic.home.overdue.phonemask

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.Dialer.Automatic
import org.simple.clinic.phone.Dialer.Manual
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.Just
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
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

  private val ui = mock<PhoneMaskBottomSheetUi>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val phoneCaller = mock<PhoneCaller>()
  private val patientUuid: UUID = UUID.fromString("90d20a06-6e8b-4f26-b317-1ac18441765d")
  private val patientRepository: PatientRepository = mock()
  private val clock = TestUserClock()

  private val patient = TestData.patient(
      uuid = patientUuid,
      fullName = "Anish Acharya",
      gender = Male,
      age = Age(30, updatedAt = Instant.now(clock)),
      dateOfBirth = null
  )
  private val phoneNumber = TestData.patientPhoneNumber(
      uuid = UUID.fromString("fe364a55-118a-4f0f-bb64-b7b0ca2e8282"),
      patientUuid = patientUuid,
      number = "1234567890"
  )

  private val proxyPhoneNumber = "0987654321"

  private lateinit var controller: PhoneMaskBottomSheetController

  @Test
  @Parameters(method = "params for making normal phone calls")
  fun `when normal call button is clicked, appropriate call should be made`(
      callTypeEvent: UiEvent,
      dialer: Dialer
  ) {
    var isCompletableSubscribed = false
    val normalCallCompletable = Completable.complete().doOnSubscribe { isCompletableSubscribed = true }
    whenever(phoneCaller.normalCall(phoneNumber.number, dialer)).doReturn(normalCallCompletable)

    sheetCreated()
    uiEvents.onNext(callTypeEvent)

    assertThat(isCompletableSubscribed).isTrue()
    verify(ui).setupView(PatientDetails(
        phoneNumber = phoneNumber.number,
        name = patient.fullName,
        gender = patient.gender,
        age = patient.age!!.value
    ))
    verify(ui).closeSheet()
    verify(ui).hideSecureCallButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  @Parameters(method = "params for making secure phone calls")
  fun `when secure call button is clicked, appropriate call should be made`(
      callTypeEvent: UiEvent,
      dialer: Dialer
  ) {
    var isCompletableSubscribed = false
    val secureCallCompletable = Completable.complete().doOnSubscribe { isCompletableSubscribed = true }
    whenever(phoneCaller.secureCall(proxyPhoneNumber, phoneNumber.number, dialer)).doReturn(secureCallCompletable)

    sheetCreated()
    uiEvents.onNext(callTypeEvent)

    assertThat(isCompletableSubscribed).isTrue()
    verify(ui).setupView(PatientDetails(
        phoneNumber = phoneNumber.number,
        name = patient.fullName,
        gender = patient.gender,
        age = patient.age!!.value
    ))
    verify(ui).closeSheet()
    verify(ui).hideSecureCallButton()
    verifyNoMoreInteractions(ui)
  }

  @Suppress("Unused")
  private fun `params for making normal phone calls`() =
      listOf(
          listOf(NormalCallClicked(permission = Just(GRANTED)), Automatic),
          listOf(NormalCallClicked(permission = Just(DENIED)), Manual)
      )

  @Suppress("Unused")
  private fun `params for making secure phone calls`() =
      listOf(
          listOf(SecureCallClicked(permission = Just(GRANTED)), Automatic),
          listOf(SecureCallClicked(permission = Just(DENIED)), Manual)
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
    verify(ui).setupView(expectedPatientDetails)
  }

  @Test
  fun `when the phone masking feature is disabled, the secure call button must be hidden`() {
    // when
    sheetCreated()

    // then
    verify(ui).hideSecureCallButton()
  }

  @Test
  fun `when the phone masking feature is enabled, the secure call button must not be hidden`() {
    // given
    val config = PhoneNumberMaskerConfig(proxyPhoneNumber = proxyPhoneNumber, phoneMaskingFeatureEnabled = true)

    // when
    sheetCreated(config = config)

    // then
    verify(ui, never()).hideSecureCallButton()
  }

  @Test
  fun `when the phone masking feature is enabled but the proxy number is not set, the secure call button must be hidden`() {
    // given
    val config = PhoneNumberMaskerConfig(proxyPhoneNumber = "", phoneMaskingFeatureEnabled = true)

    // when
    sheetCreated(config = config)

    // then
    verify(ui).hideSecureCallButton()
  }

  private fun sheetCreated(
      patient: Patient = this.patient,
      phoneNumber: PatientPhoneNumber = this.phoneNumber,
      config: PhoneNumberMaskerConfig = PhoneNumberMaskerConfig(proxyPhoneNumber = proxyPhoneNumber, phoneMaskingFeatureEnabled = false)
  ) {
    whenever(patientRepository.patient(patientUuid)).doReturn(Observable.just(patient.toOptional()))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just(phoneNumber.toOptional()))

    controller = PhoneMaskBottomSheetController(
        phoneCaller = phoneCaller,
        patientRepository = patientRepository,
        clock = clock,
        config = config
    )

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(ui) }

    uiEvents.onNext(PhoneMaskBottomSheetCreated(patientUuid))
  }
}
