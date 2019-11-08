package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toOptional

class SetupActivityEffectHandler(
    private val onboardingCompletePreference: Preference<Boolean>,
    private val uiActions: UiActions,
    private val userDao: User.RoomDao,
    private val appConfigRepository: AppConfigRepository,
    private val fallbackCountry: Country,
    private val schedulersProvider: SchedulersProvider
) {

  companion object {
    fun create(
        onboardingCompletePreference: Preference<Boolean>,
        uiActions: UiActions,
        userDao: User.RoomDao,
        appConfigRepository: AppConfigRepository,
        fallbackCountry: Country,
        schedulersProvider: SchedulersProvider
    ): ObservableTransformer<SetupActivityEffect, SetupActivityEvent> {
      return SetupActivityEffectHandler(
          onboardingCompletePreference = onboardingCompletePreference,
          uiActions = uiActions,
          userDao = userDao,
          appConfigRepository = appConfigRepository,
          fallbackCountry = fallbackCountry,
          schedulersProvider = schedulersProvider
      ).build()
    }
  }

  private fun build(): ObservableTransformer<SetupActivityEffect, SetupActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<SetupActivityEffect, SetupActivityEvent>()
        .addTransformer(FetchUserDetails::class.java, fetchUserDetails())
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
        .addTransformer(InitializeDatabase::class.java, initializeDatabase())
        .addAction(ShowCountrySelectionScreen::class.java, uiActions::showCountrySelectionScreen, schedulersProvider.ui())
        .addTransformer(SetFallbackCountryAsCurrentCountry::class.java, setFallbackCountryAsSelected())
        .build()
  }

  private fun fetchUserDetails(): ObservableTransformer<FetchUserDetails, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { readUserDetailsFromStorage() }
          .map { (hasUserCompletedOnboarding, loggedInUser, userSelectedCountry) ->
            UserDetailsFetched(hasUserCompletedOnboarding, loggedInUser, userSelectedCountry)
          }
    }
  }

  private fun readUserDetailsFromStorage(): Single<Triple<Boolean, Optional<User>, Optional<Country>>> {
    val hasUserCompletedOnboarding = Single.fromCallable { onboardingCompletePreference.get() }
    val loggedInUser = Single.fromCallable { userDao.userImmediate().toOptional() }
    val userSelectedCountry = Single.fromCallable { appConfigRepository.currentCountry() }

    return Singles
        .zip(hasUserCompletedOnboarding, loggedInUser, userSelectedCountry)
        .subscribeOn(schedulersProvider.io())
  }

  private fun initializeDatabase(): ObservableTransformer<InitializeDatabase, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { userDao.userCount().subscribeOn(schedulersProvider.io()) }
          .map { DatabaseInitialized }
    }
  }

  private fun setFallbackCountryAsSelected(): ObservableTransformer<SetFallbackCountryAsCurrentCountry, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream.flatMapSingle {
        appConfigRepository
            .saveCurrentCountry(fallbackCountry)
            .subscribeOn(schedulersProvider.io())
            .toSingleDefault(FallbackCountrySetAsSelected)
      }
    }
  }
}
