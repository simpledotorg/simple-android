package org.simple.clinic.user

import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.LoginApi
import org.simple.clinic.login.LoginErrorResponse
import org.simple.clinic.login.LoginRequest
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.UserPayload
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.FindUserResult
import org.simple.clinic.registration.RegistrationApi
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.security.pin.BruteForceProtection
import org.simple.clinic.storage.files.ClearAllFilesResult
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KClass

@AppScope
class UserSession @Inject constructor(
    private val loginApi: LoginApi,
    private val registrationApi: RegistrationApi,
    private val moshi: Moshi,
    private val facilityRepository: FacilityRepository,
    private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    private val passwordHasher: PasswordHasher,
    // This is Lazy to work around a cyclic dependency between
    // DataSync, UserSession, and PatientRepository.
    private val dataSync: Lazy<DataSync>,
    private val ongoingLoginEntryRepository: OngoingLoginEntryRepository,
    private val bruteForceProtection: BruteForceProtection,
    private val fileStorage: FileStorage,
    private val reportPendingRecords: ReportPendingRecordsToAnalytics,
    private val schedulersProvider: SchedulersProvider,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>,
    @Named("last_patient_pull_token") private val patientSyncPullToken: Preference<Optional<String>>,
    @Named("last_bp_pull_token") private val bpSyncPullToken: Preference<Optional<String>>,
    @Named("last_prescription_pull_token") private val prescriptionSyncPullToken: Preference<Optional<String>>,
    @Named("last_appointment_pull_token") private val appointmentSyncPullToken: Preference<Optional<String>>,
    @Named("last_medicalhistory_pull_token") private val medicalHistorySyncPullToken: Preference<Optional<String>>,
    @Named("onboarding_complete") private val onboardingComplete: Preference<Boolean>
) {

  private var ongoingRegistrationEntry: OngoingRegistrationEntry? = null

  fun saveOngoingLoginEntry(entry: OngoingLoginEntry): Completable {
    return ongoingLoginEntryRepository.saveLoginEntry(entry)
  }

  fun ongoingLoginEntry(): Single<OngoingLoginEntry> {
    return ongoingLoginEntryRepository.entry()
  }

  fun clearOngoingLoginEntry(): Completable {
    return ongoingLoginEntryRepository.clearLoginEntry()
  }

  fun loginWithOtp(otp: String): Single<LoginResult> {
    return ongoingLoginEntry()
        .doOnSubscribe { Timber.i("Logging in with OTP") }
        .map { LoginRequest(UserPayload(it.phoneNumber!!, it.pin!!, otp)) }
        .flatMap { loginApi.login(it) }
        .flatMap {
          storeUserAndAccessToken(it)
              .toSingleDefault(LoginResult.Success as LoginResult)
        }
        .flatMap { result ->
          reportUserLoggedInToAnalytics()
              .toSingleDefault(result)
        }
        .doOnSuccess { syncOnLoginResult() }
        .doOnSuccess { clearOngoingLoginEntry().subscribe() }
        .onErrorReturn { error ->
          when {
            error is IOException -> LoginResult.NetworkError
            error is HttpException && error.code() == 401 -> {
              val errorResponse = readErrorResponseJson(error, LoginErrorResponse::class)
              LoginResult.ServerError(errorResponse.firstError())
            }
            else -> {
              Timber.e(error)
              LoginResult.UnexpectedError
            }
          }
        }
        .doOnSuccess { Timber.i("Login result: $it") }
  }

  private fun reportUserLoggedInToAnalytics(): Completable {
    return loggedInUser()
        .firstOrError()
        .flatMapCompletable { (user) ->
          Completable.fromAction { Analytics.setLoggedInUser(user!!) }
        }
  }

  private fun syncOnLoginResult() {
    dataSync
        .get()
        .sync(null)
        .subscribeOn(schedulersProvider.io())
        .onErrorComplete()
        .subscribe()
  }

  fun saveOngoingRegistrationEntryAsUser(): Completable {
    val ongoingEntry = ongoingRegistrationEntry().cache()

    return ongoingEntry
        .doOnSubscribe { Timber.i("Logging in from ongoing registration entry") }
        .zipWith(ongoingEntry.flatMap { entry -> passwordHasher.hash(entry.pin!!) })
        .flatMapCompletable { (entry, passwordDigest) ->
          val user = User(
              uuid = entry.uuid!!,
              fullName = entry.fullName!!,
              phoneNumber = entry.phoneNumber!!,
              pinDigest = passwordDigest,
              createdAt = entry.createdAt!!,
              updatedAt = entry.createdAt,
              status = WaitingForApproval,
              loggedInStatus = NOT_LOGGED_IN)
          storeUser(user, entry.facilityId!!)
        }
  }

  fun findExistingUser(phoneNumber: String): Single<FindUserResult> {
    Timber.i("Finding user with phone number")
    return registrationApi.findUser(phoneNumber)
        .map { user -> FindUserResult.Found(user) as FindUserResult }
        .onErrorReturn { e ->
          when {
            e is IOException -> FindUserResult.NetworkError
            e is HttpException && e.code() == 404 -> FindUserResult.NotFound
            else -> {
              Timber.e(e)
              FindUserResult.UnexpectedError
            }
          }
        }
  }

  fun refreshLoggedInUser(): Completable {
    return requireLoggedInUser()
        .firstOrError()
        .doOnSuccess { Timber.i("Refreshing logged-in user") }
        .flatMapCompletable { loggedInUser ->
          registrationApi.findUser(loggedInUser.phoneNumber)
              .flatMapCompletable { userPayload ->
                // TODO: This was added to handle the case where the user logged in status will
                // not get set to LOGGED_IN when the PIN reset request is approved. See if it can
                // be done in a better way since there are many places where this sort of logic is
                // littered all over the app currently.
                val finalLoggedInStatus = if (loggedInUser.loggedInStatus == RESET_PIN_REQUESTED && userPayload.status == ApprovedForSyncing) {
                  LOGGED_IN

                } else {
                  loggedInUser.loggedInStatus
                }

                val user = userFromPayload(userPayload, finalLoggedInStatus)
                updateUser(user)
              }
        }
  }

  fun register(): Single<RegistrationResult> {
    val user: Single<User> = loggedInUser()
        .map { (user) -> user!! }
        .firstOrError()
        .cache()

    val currentFacility = user
        .flatMap { facilityRepository.facilityUuidsForUser(it).firstOrError() }

    return Singles.zip(user, currentFacility)
        .doOnSubscribe { Timber.i("Registering user") }
        .map { (user, facilityUuids) -> userToPayload(user, facilityUuids) }
        .map(::RegistrationRequest)
        .flatMap { registrationApi.createUser(it) }
        .flatMap { response ->
          storeUserAndAccessToken(response)
              .toSingleDefault(RegistrationResult.Success as RegistrationResult)
        }
        .flatMap { result ->
          reportUserRegisteredToAnalytics()
              .toSingleDefault(result)
        }
        .onErrorReturn { e ->
          Timber.e(e)
          when (e) {
            is IOException -> RegistrationResult.NetworkError
            else -> RegistrationResult.UnexpectedError
          }
        }
        .doOnSuccess { Timber.i("Registration result: $it") }
  }

  private fun reportUserRegisteredToAnalytics(): Completable {
    return loggedInUser()
        .firstOrError()
        .flatMapCompletable { (user) ->
          Completable.fromAction { Analytics.setNewlyRegisteredUser(user!!) }
        }
  }

  private fun userToPayload(user: User, facilityUuids: List<UUID>): LoggedInUserPayload {
    return user.run {
      LoggedInUserPayload(
          uuid = uuid,
          fullName = fullName,
          phoneNumber = phoneNumber,
          pinDigest = pinDigest,
          registrationFacilityId = facilityUuids.first(),
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt)
    }
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
          loggedInStatus = status)
    }
  }

  fun saveOngoingRegistrationEntry(entry: OngoingRegistrationEntry): Completable {
    return Completable.fromAction {
      this.ongoingRegistrationEntry = entry
    }
  }

  fun ongoingRegistrationEntry(): Single<OngoingRegistrationEntry> {
    return Single.fromCallable { ongoingRegistrationEntry }
  }

  fun clearOngoingRegistrationEntry(): Completable {
    return Completable.fromAction {
      ongoingRegistrationEntry = null
    }
  }

  fun isOngoingRegistrationEntryPresent(): Single<Boolean> =
      Single.fromCallable { ongoingRegistrationEntry != null }

  fun storeUserAndAccessToken(response: LoginResponse): Completable {
    Timber.i("Storing user and access token. Is token blank? ${response.accessToken.isBlank()}")
    accessTokenPreference.set(Just(response.accessToken))
    return storeUser(
        userFromPayload(response.loggedInUser, LOGGED_IN),
        response.loggedInUser.registrationFacilityId)
  }

  private fun storeUserAndAccessToken(response: ForgotPinResponse): Completable {
    Timber.i("Storing user and access token. Is token blank? ${response.accessToken.isBlank()}")
    accessTokenPreference.set(Just(response.accessToken))

    val user = userFromPayload(response.loggedInUser, RESET_PIN_REQUESTED)
    return storeUser(user, response.loggedInUser.registrationFacilityId)
  }

  private fun storeUserAndAccessToken(response: RegistrationResponse): Completable {
    Timber.i("Storing user and access token. Is token blank? ${response.accessToken.isBlank()}")
    accessTokenPreference.set(Just(response.accessToken))

    val user = userFromPayload(response.userPayload, LOGGED_IN)
    return storeUser(user, response.userPayload.registrationFacilityId)
  }

  fun storeUser(user: User, facilityUuid: UUID): Completable {
    return Completable
        .fromAction { appDatabase.userDao().createOrUpdate(user) }
        .doOnSubscribe { Timber.i("Storing user") }
        .andThen(facilityRepository.associateUserWithFacilities(user, listOf(facilityUuid), currentFacility = facilityUuid))
        .doOnError { Timber.e(it) }
  }

  fun updateUser(user: User): Completable {
    return Completable
        .fromAction { appDatabase.userDao().createOrUpdate(user) }
        .doOnSubscribe { Timber.i("Updating user") }
  }

  private fun <T : Any> readErrorResponseJson(error: HttpException, clazz: KClass<T>): T {
    val jsonAdapter = moshi.adapter(clazz.java)
    return jsonAdapter.fromJson(error.response().errorBody()!!.source())!!
  }

  fun clearLoggedInUser(): Completable {
    Timber.i("Clearing logged-in user")
    return loggedInUser()
        .firstOrError()
        .filter { it is Just<User> }
        .map { (user) -> user!! }
        .flatMapCompletable {
          Completable.fromAction {
            appDatabase.userDao().deleteUserAndFacilityMappings(it, appDatabase.userFacilityMappingDao())
          }
        }
  }

  fun logout(): Single<LogoutResult> {
    return Completable
        .concatArray(
            reportPendingRecords.report().onErrorComplete(),
            clearLocalDatabase(),
            clearSharedPreferences(),
            clearPrivateFiles()
        )
        .toSingleDefault(LogoutResult.Success as LogoutResult)
        .onErrorReturn { cause -> LogoutResult.Failure(cause) }
        .doOnSuccess { Analytics.clearUser() }
  }

  private fun clearLocalDatabase(): Completable {
    return Completable.fromAction { appDatabase.clearAllTables() }
  }

  private fun clearSharedPreferences(): Completable {
    return Completable.fromAction {
      sharedPreferences.edit().clear().apply()
      // When we clear all shared preferences, we also end up clearing the flag that states whether
      // the user has completed the onboarding flow or not. This means that if the user opens the
      // again after getting logged out and before logging in, they will be shown the Onboarding
      // screen instead of the Registration phone screen. This is a workaround that sets the flag
      // again after clearing the shared preferences to fix this.
      onboardingComplete.set(true)
    }
  }

  private fun clearPrivateFiles(): Completable {
    return Single
        .fromCallable { fileStorage.clearAllFiles() }
        .flatMapCompletable { result ->
          when (result) {
            is ClearAllFilesResult.Failure -> Completable.error(result.cause)
            else -> Completable.complete()
          }
        }
  }

  fun loggedInUser(): Observable<Optional<User>> {
    return appDatabase.userDao().user()
        .toObservable()
        .map { if (it.isEmpty()) None else Just(it.first()) }
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
  @WorkerThread
  fun loggedInUserImmediate() = appDatabase.userDao().userImmediate()

  fun isUserLoggedIn(): Boolean {
    // TODO: This is bad. Make this function return Single<Boolean> instead.
    val user = loggedInUser().blockingFirst()
    return user is Just
  }

  fun accessToken(): Optional<String> {
    return accessTokenPreference.get()
  }

  fun syncAndClearData(patientRepository: PatientRepository, syncRetryCount: Int = 0, timeoutSeconds: Long = 15L): Completable {
    Timber.i("Syncing and clearing all patient related data")

    val clearStoredPullTokens = Completable.fromAction {
      patientSyncPullToken.delete()
      bpSyncPullToken.delete()
      prescriptionSyncPullToken.delete()
      appointmentSyncPullToken.delete()
      medicalHistorySyncPullToken.delete()
    }

    return dataSync
        .get()
        .sync(null)
        .subscribeOn(schedulersProvider.io())
        .retry(syncRetryCount.toLong())
        .timeout(timeoutSeconds, TimeUnit.SECONDS)
        .onErrorComplete()
        .andThen(patientRepository.clearPatientData())
        .andThen(clearStoredPullTokens)
        .andThen(bruteForceProtection.resetFailedAttempts())
  }

  fun resetPin(pin: String): Single<ForgotPinResult> {
    val resetPasswordCall = passwordHasher.hash(pin)
        .map { ResetPinRequest(it) }
        .flatMap { loginApi.resetPin(it) }
        .doOnSubscribe { Timber.i("Resetting PIN") }

    val updateUserOnSuccess = resetPasswordCall
        .flatMapCompletable { storeUserAndAccessToken(it) }
        .toSingleDefault(ForgotPinResult.Success as ForgotPinResult)

    return updateUserOnSuccess
        .onErrorReturn {
          when (it) {
            is IOException -> ForgotPinResult.NetworkError
            is HttpException -> if (it.code() == 401) {
              ForgotPinResult.UserNotFound
            } else {
              ForgotPinResult.UnexpectedError(it)
            }
            else -> ForgotPinResult.UnexpectedError(it)
          }
        }
  }

  fun canSyncData(): Observable<Boolean> {
    return loggedInUser()
        .map { (user) ->
          when {
            user?.loggedInStatus == LOGGED_IN && user.status == ApprovedForSyncing -> true
            else -> false
          }
        }
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

  sealed class LogoutResult {
    object Success : LogoutResult()
    data class Failure(val cause: Throwable) : LogoutResult()
  }
}
