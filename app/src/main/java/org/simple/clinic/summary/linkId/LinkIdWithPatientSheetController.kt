package org.simple.clinic.summary.linkId

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LinkIdWithPatientSheet
typealias UiChange = (Ui) -> Unit

class LinkIdWithPatientSheetController @Inject constructor(
    val patientRepository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable
        .merge(
            addIdToPatient(replayedEvents),
            cancelAddingIdToPatient(replayedEvents))
  }

  private fun addIdToPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events
        .ofType<LinkIdWithPatientSheetCreated>()

    return events
        .ofType<LinkIdWithPatientAddClicked>()
        .withLatestFrom(screenCreates)
        .switchMapSingle { (_, screen) ->
          patientRepository
              .addIdentifierToPatient(screen.patientUuid, screen.identifier)
              .map { Ui::closeSheet }
        }
  }

  private fun cancelAddingIdToPatient(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<LinkIdWithPatientCancelClicked>()
        .map { Ui::closeSheet }
  }
}