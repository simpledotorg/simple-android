package org.simple.clinic.home.patients

import android.annotation.SuppressLint
import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.appupdate.AppUpdateState
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import java.time.Instant
import java.time.LocalDate
import javax.inject.Named

class PatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val refreshCurrentUser: RefreshCurrentUser,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val checkAppUpdate: CheckAppUpdateAvailability,
    private val patientRepository: PatientRepository,
    @Named("approval_status_changed_at") private val approvalStatusUpdatedAtPref: Preference<Instant>,
    @Named("approved_status_dismissed") private val hasUserDismissedApprovedStatusPref: Preference<Boolean>,
    @Named("number_of_patients_registered") private val numberOfPatientsRegisteredPref: Preference<Int>,
    @Named("app_update_last_shown_at") private val appUpdateDialogShownAtPref: Preference<Instant>,
    @Assisted private val uiActions: PatientsTabUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: PatientsTabUiActions): PatientsEffectHandler
  }

  fun build(): ObservableTransformer<PatientsTabEffect, PatientsTabEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientsTabEffect, PatientsTabEvent>()
        .addAction(OpenEnterOtpScreen::class.java, uiActions::openEnterCodeManuallyScreen, schedulers.ui())
        .addConsumer(OpenPatientSearchScreen::class.java, { uiActions.openPatientSearchScreen(it.additionalIdentifier) }, schedulers.ui())
        .addTransformer(RefreshUserDetails::class.java, refreshCurrentUser())
        .addTransformer(LoadUser::class.java, loadUser())
        .addTransformer(LoadInfoForShowingApprovalStatus::class.java, loadRequiredInfoForShowingApprovalStatus())
        .addAction(ShowUserAwaitingApproval::class.java, uiActions::showUserStatusAsWaiting, schedulers.ui())
        .addConsumer(SetDismissedApprovalStatus::class.java, { hasUserDismissedApprovedStatusPref.set(it.dismissedStatus) }, schedulers.io())
        .addAction(ShowUserWasApproved::class.java, uiActions::showUserStatusAsApproved, schedulers.ui())
        .addAction(ShowUserPendingSmsVerification::class.java, uiActions::showUserStatusAsPendingVerification, schedulers.ui())
        .addAction(HideUserAccountStatus::class.java, uiActions::hideUserAccountStatus, schedulers.ui())
        .addAction(OpenScanBpPassportScreen::class.java, uiActions::openScanSimpleIdCardScreen, schedulers.ui())
        .addTransformer(LoadNumberOfPatientsRegistered::class.java, loadNumberOfPatientsRegistered())
        .addAction(OpenTrainingVideo::class.java, uiActions::openYouTubeLinkForSimpleVideo, schedulers.ui())
        .addTransformer(LoadInfoForShowingAppUpdateMessage::class.java, loadInfoForShowingAppUpdate())
        .addConsumer(TouchAppUpdateShownAtTime::class.java, { appUpdateDialogShownAtPref.set(Instant.now(utcClock)) }, schedulers.io())
        .addAction(ShowAppUpdateAvailable::class.java, uiActions::showAppUpdateDialog, schedulers.ui())
        .build()
  }

  private fun refreshCurrentUser(): ObservableTransformer<RefreshUserDetails, PatientsTabEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { createRefreshUserCompletable() }
          .doOnNext(::runRefreshUserTask)
          .flatMap { Observable.empty<PatientsTabEvent>() }
    }
  }

  private fun createRefreshUserCompletable(): Completable {
    return refreshCurrentUser
        .refresh()
        .onErrorComplete()
  }

  @SuppressLint("CheckResult")
  private fun runRefreshUserTask(refreshUser: Completable) {
    // The refresh call should not get canceled when the screen is closed
    // (i.e., this chain gets disposed). So it's not a part of this Rx chain.
    refreshUser
        .subscribeOn(schedulers.io())
        .subscribe {
          // TODO (vs) 26/05/20: Move triggering this to the `Update` class later
          approvalStatusUpdatedAtPref.set(Instant.now(utcClock))
        }
  }

  private fun loadUser(): ObservableTransformer<LoadUser, PatientsTabEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { userSession.loggedInUser() }
          .filterAndUnwrapJust()
          .map(::UserDetailsLoaded)
    }
  }

  private fun loadRequiredInfoForShowingApprovalStatus(): ObservableTransformer<LoadInfoForShowingApprovalStatus, PatientsTabEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map {
            DataForShowingApprovedStatusLoaded(
                currentTime = Instant.now(utcClock),
                approvalStatusUpdatedAt = approvalStatusUpdatedAtPref.get(),
                hasBeenDismissed = hasUserDismissedApprovedStatusPref.get()
            )
          }
    }
  }

  private fun loadNumberOfPatientsRegistered(): ObservableTransformer<LoadNumberOfPatientsRegistered, PatientsTabEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { numberOfPatientsRegisteredPref.asObservable().subscribeOn(schedulers.io()) }
          .map(::LoadedNumberOfPatientsRegistered)
    }
  }

  private fun loadInfoForShowingAppUpdate(): ObservableTransformer<LoadInfoForShowingAppUpdateMessage, PatientsTabEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { checkAppUpdate.listen() }
          .map {
            val today = LocalDate.now(userClock)
            val updateLastShownOn = appUpdateDialogShownAtPref.get().toLocalDateAtZone(userClock.zone)

            RequiredInfoForShowingAppUpdateLoaded(
                isAppUpdateAvailable = it is AppUpdateState.ShowAppUpdate,
                appUpdateLastShownOn = updateLastShownOn,
                currentDate = today
            )
          }
    }
  }
}
