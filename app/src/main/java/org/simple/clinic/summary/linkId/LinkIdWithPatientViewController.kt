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

typealias Ui = LinkIdWithPatientView
typealias UiChange = (Ui) -> Unit

class LinkIdWithPatientViewController @Inject constructor(
    val patientRepository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable
        .merge(
            addIdToPatient(replayedEvents),
            cancelAddingIdToPatient(replayedEvents),
            displayIdentifier(replayedEvents)
        )
  }

  private fun displayIdentifier(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<LinkIdWithPatientViewShown>()
        .map { it.identifier }
        .map { identifier -> { ui: Ui -> ui.renderIdentifierText(identifier) } }
  }

  private fun addIdToPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events
        .ofType<LinkIdWithPatientViewShown>()

    return events
        .ofType<LinkIdWithPatientAddClicked>()
        .withLatestFrom(screenCreates)
        .switchMapSingle { (_, screen) ->
          patientRepository
              .addIdentifierToPatient(screen.patientUuid, screen.identifier)
              .map { { ui: Ui -> ui.closeSheetWithIdLinked() } }
        }
  }

  private fun cancelAddingIdToPatient(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<LinkIdWithPatientCancelClicked>()
        .map { { ui: Ui -> ui.closeSheetWithoutIdLinked() } }
  }
}
