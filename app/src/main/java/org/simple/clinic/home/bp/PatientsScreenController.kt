package org.simple.clinic.home.bp

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientsScreen
typealias UiChange = (Ui) -> Unit

class PatientsScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    return Observable.merge(
        newPatientClicks(replayedEvents),
        checkApprovalStatusOnStart(replayedEvents))
  }

  private fun newPatientClicks(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events.ofType(NewPatientClicked::class.java)
        .map { { ui: PatientsScreen -> ui.openNewPatientScreen() } }
  }

  private fun checkApprovalStatusOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    // Skipping the first resume because it'll be a duplicate of screen-created.
    val screenStarts = events.ofType<ScreenCreated>()
    val screenResumes = events.ofType<TheActivityLifecycle.Resumed>().skip(1)

    return Observable.merge(screenStarts, screenResumes)
        .flatMap {
          userSession.loggedInUser()
              .take(1)
              .map { (user) -> user!! }
              .filter { user -> user.status == UserStatus.WAITING_FOR_APPROVAL }
              .flatMapCompletable { userSession.refreshLoggedInUser() }
              .andThen(Observable.never<UiChange>())
        }
  }
}
