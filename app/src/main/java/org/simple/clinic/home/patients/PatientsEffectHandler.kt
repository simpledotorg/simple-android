package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.Instant
import javax.inject.Named

class PatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val refreshCurrentUser: RefreshCurrentUser,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    @Named("approval_status_changed_at") private val approvalStatusUpdatedAtPref: Preference<Instant>,
    @Named("approved_status_dismissed") private val hasUserDismissedApprovedStatusPref: Preference<Boolean>,
    @Assisted private val uiActions: PatientsUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: PatientsUiActions): PatientsEffectHandler
  }

  fun build(): ObservableTransformer<PatientsEffect, PatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientsEffect, PatientsEvent>()
        .addAction(OpenEnterOtpScreen::class.java, uiActions::openEnterCodeManuallyScreen, schedulers.ui())
        .addAction(OpenPatientSearchScreen::class.java, uiActions::openPatientSearchScreen, schedulers.ui())
        .addTransformer(RefreshUserDetails::class.java, refreshCurrentUser())
        .addTransformer(LoadUser::class.java, loadUser())
        .addTransformer(LoadDismissedApprovalStatus::class.java, loadDismissedApprovalStatus())
        .addAction(ShowUserAwaitingApproval::class.java, uiActions::showUserStatusAsWaiting, schedulers.ui())
        .addConsumer(SetDismissedApprovalStatus::class.java, { hasUserDismissedApprovedStatusPref.set(it.dismissedStatus) }, schedulers.io())
        .build()
  }

  private fun refreshCurrentUser(): ObservableTransformer<RefreshUserDetails, PatientsEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            // TODO (vs) 26/05/20: Make this a blocking call
            refreshCurrentUser
                .refresh()
                .subscribeOn(schedulers.io())
                .onErrorComplete()
                .doOnComplete {
                  // TODO (vs) 26/05/20: Move triggering this to the `Update` class later
                  approvalStatusUpdatedAtPref.set(Instant.now(utcClock))
                }
                .andThen(Observable.empty<PatientsEvent>())
          }
    }
  }

  private fun loadUser(): ObservableTransformer<LoadUser, PatientsEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { userSession.loggedInUser() }
          .filterAndUnwrapJust()
          .map(::UserDetailsLoaded)
    }
  }

  private fun loadDismissedApprovalStatus(): ObservableTransformer<LoadDismissedApprovalStatus, PatientsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { hasUserDismissedApprovedStatusPref.get() }
          .map(::DismissedApprovalStatusLoaded)
    }
  }
}
