package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider

object SetupActivityEffectHandler {

  fun create(
      onboardingCompletePreference: Preference<Boolean>,
      uiActions: UiActions,
      userDao: User.RoomDao,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<SetupActivityEffect, SetupActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<SetupActivityEffect, SetupActivityEvent>()
        .addTransformer(FetchUserDetails::class.java, fetchUserDetails(onboardingCompletePreference, schedulersProvider.io()))
        .addAction(GoToMainActivity::class.java, uiActions::goToMainActivity, schedulersProvider.ui())
        .addAction(ShowOnboardingScreen::class.java, uiActions::showOnboardingScreen, schedulersProvider.ui())
        // We could technically also implicitly wait on the database to
        // initialize by querying the actual user data and make it a
        // property of the FetchUserDetails effect, but that has the
        // problem of making it hidden behaviour.

        // If the user details is removed from this screen in the
        // future, then we will also lose the feature of this screen
        // showing the placeholder UI until the database is migrated.

        // In this case, it might be better to have this as an explicit
        // effect so that the intention is clear.
        .addTransformer(InitializeDatabase::class.java, initializeDatabase(userDao, schedulersProvider.io()))
        .build()
  }

  private fun fetchUserDetails(
      onboardingCompletePreference: Preference<Boolean>,
      scheduler: Scheduler
  ): ObservableTransformer<FetchUserDetails, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { Single.just(onboardingCompletePreference.get()) }
          .subscribeOn(scheduler)
          .map(::UserDetailsFetched)
    }
  }

  private fun initializeDatabase(
      userDao: User.RoomDao,
      scheduler: Scheduler
  ): ObservableTransformer<InitializeDatabase, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { userDao.userCount().subscribeOn(scheduler) }
          .map { DatabaseInitialized }
    }
  }
}
