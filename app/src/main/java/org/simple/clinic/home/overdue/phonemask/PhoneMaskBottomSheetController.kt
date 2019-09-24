package org.simple.clinic.home.overdue.phonemask

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer.Automatic
import org.simple.clinic.phone.Dialer.Manual
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissionResult.NEVER_ASK_AGAIN
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PhoneMaskBottomSheet
private typealias UiChange = (Ui) -> Unit

class PhoneMaskBottomSheetController @Inject constructor(
    private val phoneCaller: PhoneCaller,
    private val patientRepository: PatientRepository,
    private val clock: UserClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        setupView(replayedEvents),
        requestCallPermissionForNormalCalls(replayedEvents),
        requestCallPermissionForSecureCalls(replayedEvents),
        makeNormalCall(replayedEvents),
        makeSecureCall(replayedEvents)
    )
  }

  private fun setupView(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuidStream = events
        .ofType<PhoneMaskBottomSheetCreated>()
        .map { it.patientUuid }

    val patientStream = patientUuidStream
        .flatMap(patientRepository::patient)
        .unwrapJust()

    val patientPhoneNumberStream = patientUuidStream
        .flatMap(patientRepository::phoneNumber)
        .unwrapJust()

    return Observables.combineLatest(patientStream, patientPhoneNumberStream)
        .map { (patient, phoneNumber) -> patientDetails(patient, phoneNumber) }
        .map { patientDetails ->
          { ui: Ui -> ui.setupView(patientDetails) }
        }
  }

  private fun patientDetails(patient: Patient, phoneNumber: PatientPhoneNumber): PatientDetails {
    return PatientDetails(
        phoneNumber = phoneNumber.number,
        name = patient.fullName,
        gender = patient.gender,
        age = ageValue(patient)
    )
  }

  private fun ageValue(patient: Patient): Int {
    return DateOfBirth.fromPatient(patient, clock).estimateAge(clock)
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
          .flatMap { (permissionResult, _, phoneNumber) ->
            phoneCaller.normalCall(phoneNumber, dialer(permissionResult))
                .andThen(Observable.just(Ui::closeSheet))
          }

  private fun makeSecureCall(events: Observable<UiEvent>) =
      callPermissionResult(events)
          .withLatestFrom(secureCallClicked(events), patientPhoneNumberStream(events))
          .flatMap { (permissionResult, _, phoneNumber) ->
            phoneCaller.secureCall(phoneNumber, dialer(permissionResult))
                .andThen(Observable.just(Ui::closeSheet))
          }

  private fun callPermissionResult(events: Observable<UiEvent>) =
      events
          .ofType<CallPhonePermissionChanged>()
          .map { it.result }

  private fun normalCallClicked(events: Observable<UiEvent>) =
      events.ofType<NormalCallClicked>()

  private fun secureCallClicked(events: Observable<UiEvent>) =
      events.ofType<SecureCallClicked>()

  private fun dialer(permissionResult: RuntimePermissionResult) =
      when (permissionResult) {
        GRANTED -> Automatic
        DENIED, NEVER_ASK_AGAIN -> Manual
      }

  private fun patientPhoneNumberStream(events: Observable<UiEvent>): Observable<String> {
    return events
        .ofType<PhoneMaskBottomSheetCreated>()
        .flatMap { patientRepository.phoneNumber(it.patientUuid) }
        .unwrapJust()
        .map { it.number }
  }
}
