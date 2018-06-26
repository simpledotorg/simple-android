package org.simple.clinic.user

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.di.AppScope
import org.simple.clinic.login.LoginApiV1
import org.simple.clinic.login.LoginRequest
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.UserPayload
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@AppScope
class UserSession @Inject constructor(
    private val api: LoginApiV1,
    private val loggedInUserPreference: Preference<Optional<LoggedInUser>>,
    @Named("preference_access_token") private val accessTokenPreference: Preference<Optional<String>>
) {

  private lateinit var ongoingLoginEntry: OngoingLoginEntry

  fun saveOngoingLoginEntry(entry: OngoingLoginEntry): Completable {
    return Completable.fromAction {
      this.ongoingLoginEntry = entry
    }
  }

  fun ongoingLoginEntry(): Single<OngoingLoginEntry> {
    return Single.fromCallable { ongoingLoginEntry }
  }

  fun login(): Single<LoginResult> {
    return ongoingLoginEntry()
        .map { LoginRequest(UserPayload(it.phoneNumber!!, it.pin!!, it.otp)) }
        .flatMap { api.login(it) }
        .map { toResult(it) }
  }

  private fun toResult(response: Response<LoginResponse>): LoginResult {
    return when {
      response.isSuccessful -> storeUserAndReturnSuccess(response)
      response is IOException -> LoginResult.NetworkError()
      else -> LoginResult.ServerError()
    }
  }

  private fun storeUserAndReturnSuccess(response: Response<LoginResponse>): LoginResult {
    accessTokenPreference.set(Just(response.body()!!.accessToken))
    loggedInUserPreference.set(Just(response.body()!!.loggedInUser))
    return LoginResult.Success()
  }
}
