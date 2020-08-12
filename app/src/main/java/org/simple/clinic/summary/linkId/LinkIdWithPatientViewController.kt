package org.simple.clinic.summary.linkId

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LinkIdWithPatientViewUi
typealias UiChange = (Ui) -> Unit

class LinkIdWithPatientViewController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val uuidGenerator: UuidGenerator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
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

    return events
        .ofType<LinkIdWithPatientAddClicked>()
        .withLatestFrom(screenCreates, currentUserStream) { _, screenCreated, loggedInUser -> Pair(screenCreated, loggedInUser) }
        .switchMapSingle { (screen, loggedInUser) ->
          patientRepository
              .addIdentifierToPatient(
                  uuid = uuidGenerator.v4(),
                  patientUuid = screen.patientUuid,
                  identifier = screen.identifier,
                  assigningUser = loggedInUser
              )
              .map { { ui: Ui -> ui.closeSheetWithIdLinked() } }
        }
  }

  private fun cancelAddingIdToPatient(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<LinkIdWithPatientCancelClicked>()
        .map { { ui: Ui -> ui.closeSheetWithoutIdLinked() } }
  }
}
