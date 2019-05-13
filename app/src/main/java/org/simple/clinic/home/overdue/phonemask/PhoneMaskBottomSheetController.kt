package org.simple.clinic.home.overdue.phonemask

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.phone.Caller.UsingDialer
import org.simple.clinic.phone.Caller.WithoutDialer
import org.simple.clinic.phone.MaskedPhoneCaller
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PhoneMaskBottomSheet
private typealias UiChange = (Ui) -> Unit

class PhoneMaskBottomSheetController @Inject constructor(
    private val phoneCaller: MaskedPhoneCaller
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        requestCallPermissionForNormalCalls(replayedEvents),
        requestCallPermissionForSecureCalls(replayedEvents),
        makeNormalCall(replayedEvents),
        makeSecureCall(replayedEvents)
    )
  }

  private fun requestCallPermissionForNormalCalls(events: Observable<UiEvent>) =
      normalCallClicked(events)
          .map { Ui::requestCallPermission }

  private fun requestCallPermissionForSecureCalls(events: Observable<UiEvent>) =
      secureCallClicked(events)
          .map { Ui::requestCallPermission }

  private fun makeNormalCall(events: Observable<UiEvent>) =
      callPermissionResult(events)
          .withLatestFrom(normalCallClicked(events), patientPhoneNumberStream(events))
          .flatMapCompletable { (permissionResult, _, phoneNumber) ->
            phoneCaller.normalCall(phoneNumber, caller(permissionResult))
          }
          .andThen(Observable.empty<UiChange>())

  private fun makeSecureCall(events: Observable<UiEvent>) =
      callPermissionResult(events)
          .withLatestFrom(secureCallClicked(events), patientPhoneNumberStream(events))
          .flatMapCompletable { (permissionResult, _, phoneNumber) ->
            phoneCaller.maskedCall(phoneNumber, caller(permissionResult))
          }
          .andThen(Observable.empty<UiChange>())

  private fun callPermissionResult(events: Observable<UiEvent>) =
      events
          .ofType<CallPhonePermissionChanged>()
          .map { it.result }

  private fun normalCallClicked(events: Observable<UiEvent>) =
      events.ofType<NormalCallClicked>()

  private fun secureCallClicked(events: Observable<UiEvent>) =
      events.ofType<SecureCallClicked>()

  private fun caller(permissionResult: RuntimePermissionResult) =
      when (permissionResult) {
        GRANTED -> WithoutDialer
        DENIED, NEVER_ASK_AGAIN -> UsingDialer
      }

  private fun patientPhoneNumberStream(events: Observable<UiEvent>) =
      events.ofType<PhoneMaskBottomSheetCreated>()
          .map { it.patient }
          .filter { it.phoneNumber != null }
          .map { it.phoneNumber!! }
}
