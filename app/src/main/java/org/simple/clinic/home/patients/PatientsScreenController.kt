package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.APPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.DISAPPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.WAITING_FOR_APPROVAL
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientsScreen
typealias UiChange = (Ui) -> Unit

class PatientsScreenController @Inject constructor(
    private val userSession: UserSession,
    @Named("approval_status_changed_at") private val approvalStatusUpdatedAtPref: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    return Observable.merge(
        newPatientClicks(replayedEvents),
        refreshApprovalStatusOnStart(replayedEvents),
        showApprovalStatus(replayedEvents))
  }

  private fun newPatientClicks(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events.ofType(NewPatientClicked::class.java)
        .map { { ui: PatientsScreen -> ui.openNewPatientScreen() } }
  }

  private fun refreshApprovalStatusOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    // Skipping the first resume because it'll be a duplicate of screen-created.
    val screenCreate = events.ofType<ScreenCreated>()
    val screenResumes = events.ofType<TheActivityLifecycle.Resumed>().skip(1)

    return Observable.merge(screenCreate, screenResumes)
        .flatMap {
          userSession.loggedInUser()
              .take(1)
              .filter { (user) -> user!!.status == WAITING_FOR_APPROVAL }
              .flatMapCompletable { userSession.refreshLoggedInUser() }
              .andThen(Observable.never<UiChange>())
        }
  }

  private fun showApprovalStatus(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMap {
          userSession.loggedInUser()
              .map { (user) -> user!! }
              .map {
                when (it.status) {
                  WAITING_FOR_APPROVAL -> { ui: Ui -> ui.showUserStatusAsWaiting() }
                  DISAPPROVED_FOR_SYNCING -> { ui: Ui -> ui.hideUserApprovalStatus() }
                  APPROVED_FOR_SYNCING -> {
                    val twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS)
                    val wasApprovedInLast24Hours = twentyFourHoursAgo < approvalStatusUpdatedAtPref.get()

                    if (wasApprovedInLast24Hours) {
                      { ui: Ui -> ui.showUserStatusAsApproved() }
                    } else {
                      { ui: Ui -> ui.hideUserApprovalStatus() }
                    }
                  }
                }
              }
        }
  }
}
