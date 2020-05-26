package org.simple.clinic.home.patients

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val refreshCurrentUser: RefreshCurrentUser,
    private val userSession: UserSession,
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
                .andThen(Observable.fromCallable { userSession.loggedInUserImmediate()!! })
          }
          .map(::UserDetailsRefreshed)
    }
  }
}
