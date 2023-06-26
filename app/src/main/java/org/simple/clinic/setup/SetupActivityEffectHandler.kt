package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.DATABASE_NAME
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DatabaseMaintenanceRunAt
import org.simple.clinic.main.TypedPreference.Type.OnboardingComplete
import org.simple.clinic.setup.runcheck.AllowApplicationToRun
import org.simple.clinic.storage.DatabaseEncryptor
import org.simple.clinic.user.User
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toOptional
import java.time.Instant
import java.util.Optional

class SetupActivityEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: UiActions,
    private val userDao: User.RoomDao,
    private val appConfigRepository: AppConfigRepository,
    private val schedulersProvider: SchedulersProvider,
    private val appDatabase: AppDatabase,
    private val clock: UtcClock,
    private val allowApplicationToRun: AllowApplicationToRun,
    @TypedPreference(OnboardingComplete) private val onboardingCompletePreference: Preference<Boolean>,
    @TypedPreference(DatabaseMaintenanceRunAt) private val databaseMaintenanceRunAt: Preference<Optional<Instant>>,
    private val userClock: UserClock,
    private val loadV1Country: LoadV1Country,
    private val databaseEncryptor: DatabaseEncryptor
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): SetupActivityEffectHandler
  }

  fun build(): ObservableTransformer<SetupActivityEffect, SetupActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<SetupActivityEffect, SetupActivityEvent>()
        .addTransformer(FetchUserDetails::class.java, fetchUserDetails(schedulersProvider.io()))
        .addAction(GoToMainActivity::class.java, uiActions::goToMainActivity, schedulersProvider.ui())
        .addAction(ShowOnboardingScreen::class.java, uiActions::showSplashScreen, schedulersProvider.ui())
        // We could technically also implicitly wait on the database to
        // initialize by querying the actual user data and make it a
        // property of the FetchUserDetails effect, but that has the
        // problem of making it hidden behaviour.

        // If the user details is removed from this screen in the
        // future, then we will also lose the feature of this screen
        // showing the placeholder UI until the database is migrated.

        // In this case, it might be better to have this as an explicit
        // effect so that the intention is clear.
        .addTransformer(InitializeDatabase::class.java, initializeDatabase(schedulersProvider.io()))
        .addAction(ShowCountrySelectionScreen::class.java, uiActions::showCountrySelectionScreen, schedulersProvider.ui())
        .addTransformer(RunDatabaseMaintenance::class.java, runDatabaseMaintenance())
        .addTransformer(FetchDatabaseMaintenanceLastRunAtTime::class.java, loadLastDatabaseMaintenanceTime())
        .addConsumer(ShowNotAllowedToRunMessage::class.java, { uiActions.showDisallowedToRunError(it.reason) }, schedulersProvider.ui())
        .addTransformer(CheckIfAppCanRun::class.java, checkApplicationAllowedToRun())
        .addTransformer(SaveCountryAndDeployment::class.java, saveCountryAndDeployment())
        .addTransformer(DeleteStoredCountryV1::class.java, deleteStoredCountryV1())
        .addTransformer(ExecuteDatabaseEncryption::class.java, executeDatabaseEncryption())
        .build()
  }

  private fun executeDatabaseEncryption(): ObservableTransformer<ExecuteDatabaseEncryption, SetupActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { databaseEncryptor.execute(DATABASE_NAME) }
          .map { DatabaseEncryptionFinished }
    }
  }

  private fun deleteStoredCountryV1(): ObservableTransformer<DeleteStoredCountryV1, SetupActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .subscribeOn(schedulersProvider.io())
          .map { appConfigRepository.deleteStoredCountryV1() }
          .map { StoredCountryV1Deleted }
    }
  }

  private fun fetchUserDetails(scheduler: Scheduler): ObservableTransformer<FetchUserDetails, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { Single.fromCallable(::readUserDetailsFromStorage).subscribeOn(scheduler) }
          .map { (hasUserCompletedOnboarding, loggedInUser, userSelectedCountry) ->
            UserDetailsFetched(
                hasUserCompletedOnboarding = hasUserCompletedOnboarding,
                loggedInUser = loggedInUser,
                userSelectedCountry = userSelectedCountry,
                userSelectedCountryV1 = loadV1Country.load(),
                currentDeployment = appConfigRepository.currentDeployment().toOptional()
            )
          }
    }
  }

  private fun readUserDetailsFromStorage(): Triple<Boolean, Optional<User>, Optional<Country>> {
    val hasUserCompletedOnboarding = onboardingCompletePreference.get()
    val loggedInUser = userDao.userImmediate().toOptional()
    val userSelectedCountry = appConfigRepository.currentCountry()

    return Triple(hasUserCompletedOnboarding, loggedInUser, userSelectedCountry.toOptional())
  }

  private fun initializeDatabase(scheduler: Scheduler): ObservableTransformer<InitializeDatabase, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { userDao.userCount().subscribeOn(scheduler) }
          .map { DatabaseInitialized }
    }
  }

  private fun runDatabaseMaintenance(): ObservableTransformer<RunDatabaseMaintenance, SetupActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { appDatabase.prune(now = Instant.now(userClock)) }
          .doOnNext { databaseMaintenanceRunAt.set(Optional.of(Instant.now(clock))) }
          .map { DatabaseMaintenanceCompleted }
    }
  }

  private fun loadLastDatabaseMaintenanceTime(): ObservableTransformer<FetchDatabaseMaintenanceLastRunAtTime, SetupActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { databaseMaintenanceRunAt.get() }
          .map(::DatabaseMaintenanceLastRunAtTimeLoaded)
    }
  }

  private fun checkApplicationAllowedToRun(): ObservableTransformer<CheckIfAppCanRun, SetupActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { allowApplicationToRun.check() }
          .map(::AppAllowedToRunCheckCompleted)
    }
  }

  private fun saveCountryAndDeployment(): ObservableTransformer<SaveCountryAndDeployment, SetupActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { effect ->
            appConfigRepository.saveCurrentCountry(effect.country)
            appConfigRepository.saveDeployment(effect.deployment)
          }
          .map { CountryAndDeploymentSaved }
    }
  }
}
