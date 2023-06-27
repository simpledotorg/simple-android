package org.simple.clinic.user

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.annotation.WorkerThread
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.parcelize.Parcelize
import org.simple.clinic.AppDatabase
import org.simple.clinic.appconfig.Country
import org.simple.clinic.di.AppScope
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.OnboardingComplete
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.plumbing.infrastructure.Infrastructure
import org.simple.clinic.plumbing.infrastructure.UpdateInfrastructureUserDetails
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.storage.SharedPreferencesMode
import org.simple.clinic.storage.SharedPreferencesMode.Mode.Default
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.filterAndUnwrapJust
import timber.log.Timber
import java.util.Optional
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@AppScope
class UserSession @Inject constructor(
    @SharedPreferencesMode(Default) private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    private val passwordHasher: PasswordHasher,
    private val ongoingLoginEntryRepository: OngoingLoginEntryRepository,
    private val reportPendingRecords: ReportPendingRecordsToAnalytics,
    private val selectedCountryPreference: Preference<Optional<Country>>,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>,
    @TypedPreference(OnboardingComplete) private val onboardingComplete: Preference<Boolean>,
    private val infrastructures: List<@JvmSuppressWildcards Infrastructure>
) {

  @Deprecated(message = "Use OngoingLoginEntryRepository directly.")
  fun saveOngoingLoginEntry(entry: OngoingLoginEntry): Completable {
    return ongoingLoginEntryRepository.saveLoginEntry(entry)
  }

  @Deprecated(message = "Use OngoingLoginEntryRepository directly.")
  fun ongoingLoginEntry(): Single<OngoingLoginEntry> {
    return ongoingLoginEntryRepository.entry()
  }

  @Deprecated(message = "Use OngoingLoginEntryRepository directly.")
  fun clearOngoingLoginEntry() {
    ongoingLoginEntryRepository.clearLoginEntry()
  }

  private fun userFromPayload(payload: LoggedInUserPayload, status: User.LoggedInStatus): User {
    return payload.run {
      User(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          status = this.status,
          createdAt = createdAt,
          updatedAt = updatedAt,
          loggedInStatus = status,
          registrationFacilityUuid = payload.registrationFacilityId,
          currentFacilityUuid = payload.registrationFacilityId,
          teleconsultPhoneNumber = payload.teleconsultPhoneNumber,
          capabilities = payload.capabilities
      )
    }
  }

  fun storeUserAndAccessToken(userPayload: LoggedInUserPayload, accessToken: String): Completable {
    accessTokenPreference.set(Optional.of(accessToken))
    return storeUser(
        userFromPayload(userPayload, LOGGED_IN)
    )
  }

  fun storeUser(user: User): Completable {
    return Completable
        .fromAction { appDatabase.userDao().createOrUpdate(user) }
        .doOnSubscribe { Timber.i("Storing user") }
        .doOnError { Timber.e(it) }
  }

  fun clearLoggedInUser(): Completable {
    Timber.i("Clearing logged-in user")
    return loggedInUser()
        .firstOrError()
        .extractIfPresent()
        .flatMapCompletable {
          Completable.fromAction {
            appDatabase.userDao().deleteUser(it)
          }
        }
  }

  fun logout(): Single<LogoutResult> {
    return Completable
        .concatArray(
            reportPendingRecords.report().onErrorComplete(),
            clearLocalDatabase(),
            clearSharedPreferences()
        )
        .toSingleDefault(LogoutResult.Success as LogoutResult)
        .onErrorReturn { cause -> LogoutResult.Failure(cause) }
        .doOnSuccess {
          Analytics.clearUser()
          infrastructures.forEach { infrastructure -> infrastructure.clear() }
        }
  }

  private fun clearLocalDatabase(): Completable {
    return Completable.fromAction { appDatabase.clearAllTables() }
  }

  private fun clearSharedPreferences(): Completable {
    return Completable.fromAction {
      // Retain the saved country when clearing the shared preferences
      val savedCountryData = sharedPreferences.getString(selectedCountryPreference.key(), "")

      sharedPreferences.edit().clear().apply()
      // When we clear all shared preferences, we also end up clearing the flag that states whether
      // the user has completed the onboarding flow or not. This means that if the user opens the
      // again after getting logged out and before logging in, they will be shown the Onboarding
      // screen instead of the Registration phone screen. This is a workaround that sets the flag
      // again after clearing the shared preferences to fix this.
      onboardingComplete.set(true)
      sharedPreferences.edit().putString(selectedCountryPreference.key(), savedCountryData).apply()
    }
  }

  fun loggedInUser(): Observable<Optional<User>> {
    return appDatabase.userDao().user()
        .toObservable()
        .map { if (it.isEmpty()) Optional.empty() else Optional.of(it.first()) }
  }

  @Deprecated(
      message = "User can get logged out now and cleared; Don't use this function anymore",
      replaceWith = ReplaceWith("loggedInUser()")
  )
  fun requireLoggedInUser(): Observable<User> {
    /*
    * The earlier version of this method used to throw an error if the returned value from
    * loggedInUser() was None, but we have added the logout feature which clears the database in the
    * background.
    *
    * This means that subscribers can no longer assume that the user will ALWAYS be present in their
    * specific use case, and must explicitly choose to either ignore or handle a User being None.
    *
    * Since this is not a very frequent case that a user will be logged out, we can change this
    * function to instead just not emit anything (instead of throwing an exception) if the user gets
    * cleared and deprecate this function. Callers can choose to migrate in stages since this
    * function is being used EVERYWHERE.
    **/
    return loggedInUser().filterAndUnwrapJust()
  }

  // FIXME: Figure out a better way to add access tokens to network calls in a reactive fashion
  // FIXME: Maybe add a separate RxCallAdapterFactory that lets us transform requests without interceptors?
  // This was added because we needed to call it from the OkHttp Interceptor
  // in a synchronous fashion because the Rx - blocking() call used earlier
  // was causing a deadlock in the Room threads when data sync happened
  fun loggedInUserImmediate() = appDatabase.userDao().userImmediate()

  @WorkerThread
  fun isUserPresentLocally(): Boolean {
    return loggedInUserImmediate() != null
  }

  fun accessToken(): Optional<String> {
    return accessTokenPreference.get()
  }

  fun unauthorize(): Completable {
    return loggedInUser()
        .filterAndUnwrapJust()
        .firstOrError()
        .flatMapCompletable { user -> updateLoggedInStatusForUser(user.uuid, UNAUTHORIZED) }
  }

  fun isUserUnauthorized(): Observable<Boolean> {
    return loggedInUser()
        .filterAndUnwrapJust()
        .map { user -> user.loggedInStatus == UNAUTHORIZED }
        .distinctUntilChanged()
  }

  fun isUserDisapproved(): Observable<Boolean> {
    return loggedInUser()
        .filterAndUnwrapJust()
        .map { user -> user.status == UserStatus.DisapprovedForSyncing }
        .distinctUntilChanged()
  }

  fun updateLoggedInStatusForUser(
      userUuid: UUID,
      newLoggedInStatus: User.LoggedInStatus
  ): Completable {
    return Completable.fromAction {
      appDatabase
          .userDao()
          .updateLoggedInStatusForUser(userUuid, newLoggedInStatus)
    }
  }

  @WorkerThread
  fun userFacilityDetails(): UserFacilityDetails? {
    return appDatabase.userDao().userAndFacilityDetails()
  }

  sealed class LogoutResult : Parcelable {

    @Parcelize
    object Success : LogoutResult()

    @Parcelize
    data class Failure(val cause: Throwable) : LogoutResult()
  }
}
