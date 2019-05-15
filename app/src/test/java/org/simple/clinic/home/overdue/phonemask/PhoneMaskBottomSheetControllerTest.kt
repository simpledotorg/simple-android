package org.simple.clinic.home.overdue.phonemask

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientMocker.overduePatient
import org.simple.clinic.phone.Caller
import org.simple.clinic.phone.Caller.UsingDialer
import org.simple.clinic.phone.Caller.WithoutDialer
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class PhoneMaskBottomSheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<PhoneMaskBottomSheet>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val phoneCaller = mock<PhoneCaller>()
  private val controller = PhoneMaskBottomSheetController(phoneCaller)

  @Before
  fun setUp() {
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  @Parameters(method = "params for types of call")
  fun `when any call button is clicked, call permission should be requested`(callTypeEvent: UiEvent) {
    uiEvents.onNext(PhoneMaskBottomSheetCreated(overduePatient(phoneNumber = "1234567890")))
    uiEvents.onNext(callTypeEvent)

    verify(screen).requestCallPermission()
    verifyNoMoreInteractions(screen)
  }

  @Suppress("Unused")
  private fun `params for types of call`() =
      listOf(NormalCallClicked, SecureCallClicked)

  @Test
  @Parameters(method = "params for making phone calls")
  fun `when any call button is clicked and permission result is received, appropriate call should be made`(
      callTypeEvent: UiEvent,
      permission: RuntimePermissionResult,
      caller: Caller
  ) {
    val number = "1234567890"

    whenever(phoneCaller.normalCall(number, caller)).thenReturn(Completable.complete())
    whenever(phoneCaller.secureCall(number, caller)).thenReturn(Completable.complete())

    uiEvents.onNext(PhoneMaskBottomSheetCreated(overduePatient(phoneNumber = number)))
    uiEvents.onNext(callTypeEvent)
    uiEvents.onNext(CallPhonePermissionChanged(permission))

    verify(screen).requestCallPermission()
    verifyNoMoreInteractions(screen)
  }

  private fun `params for making phone calls`() =
      listOf(
          listOf(NormalCallClicked, GRANTED, WithoutDialer),
          listOf(NormalCallClicked, DENIED, UsingDialer),
          listOf(NormalCallClicked, NEVER_ASK_AGAIN, UsingDialer),
          listOf(SecureCallClicked, GRANTED, WithoutDialer),
          listOf(SecureCallClicked, DENIED, UsingDialer),
          listOf(SecureCallClicked, NEVER_ASK_AGAIN, UsingDialer)
      )
}
