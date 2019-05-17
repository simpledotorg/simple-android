package org.simple.clinic.summary.linkId

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LinkIdWithPatientView
typealias UiChange = (Ui) -> Unit

class LinkIdWithPatientViewController @Inject constructor(
    val patientRepository: PatientRepository,
    val userSession: UserSession,
    val facilityRepository: FacilityRepository
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

    val currentUserStream = userSession
        .requireLoggedInUser()
        .replay()
        .refCount()

    val currentFacilityStream = currentUserStream
        .flatMap { loggedInUser -> facilityRepository.currentFacility(loggedInUser) }

    return events
        .ofType<LinkIdWithPatientAddClicked>()
        .withLatestFrom(screenCreates, currentUserStream, currentFacilityStream) { _, screenCreated, loggedInUser, currentFacility ->
          Triple(screenCreated, loggedInUser, currentFacility)

        }
        .switchMapSingle { (screen, loggedInUser, currentFacility) ->
          patientRepository
              .addIdentifierToPatient(screen.patientUuid, screen.identifier, loggedInUser, currentFacility)
              .map { { ui: Ui -> ui.closeSheetWithIdLinked() } }
        }
  }

  private fun cancelAddingIdToPatient(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<LinkIdWithPatientCancelClicked>()
        .map { { ui: Ui -> ui.closeSheetWithoutIdLinked() } }
  }
}
