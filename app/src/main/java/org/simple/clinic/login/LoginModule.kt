package org.simple.clinic.login

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.forgotpin.ForgotPinApiV1
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.login.applock.BCryptPasswordHasher
import org.simple.clinic.login.applock.PasswordHasher
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Module
open class LoginModule {

  @Provides
  fun loginApi(retrofit: Retrofit): LoginApiV1 {
    return retrofit.create(LoginApiV1::class.java)
  }

  // TODO: This is temporary until the api is ready. Remove later.
  @Provides
  fun forgotPinApi(appDatabase: AppDatabase, @Named("preference_access_token") accessToken: Preference<Optional<String>>): ForgotPinApiV1 {
    return object : ForgotPinApiV1 {

      val userDao = appDatabase.userDao()
      val userFacilityMappingDao = appDatabase.userFacilityMappingDao()

      override fun resetPin(request: ResetPinRequest): Single<ForgotPinResponse> {
        return Single.fromCallable { userDao.userImmediate() }
            .delay(2L, TimeUnit.SECONDS)
            .map {
              LoggedInUserPayload(
                  uuid = it.uuid,
                  fullName = it.fullName,
                  phoneNumber = it.phoneNumber,
                  pinDigest = request.passwordDigest,
                  facilityUuids = userFacilityMappingDao.facilityUuids(it.uuid).blockingFirst(),
                  status = UserStatus.WAITING_FOR_APPROVAL,
                  createdAt = it.createdAt,
                  updatedAt = it.updatedAt
              )
            }
            .map { ForgotPinResponse(accessToken = accessToken.get().toNullable()!!, loggedInUser = it) }
      }
    }
  }

  @Provides
  @Named("preference_access_token")
  fun accessTokenForLoggedInUser(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("access_token", None, OptionalRxPreferencesConverter(object : Preference.Converter<String> {
      override fun deserialize(serialized: String): String {
        return serialized
      }

      override fun serialize(value: String): String {
        return value
      }
    }))
  }

  @Provides
  fun passwordHasher(): PasswordHasher {
    return BCryptPasswordHasher()
  }

  @Provides
  open fun appLockConfig(): Single<AppLockConfig> {
    return Single.just(AppLockConfig(lockAfterTimeMillis = TimeUnit.MINUTES.toMillis(15)))
  }

  @Provides
  open fun loginConfig(): Single<LoginConfig> = Single.just(LoginConfig(isOtpLoginFlowEnabled = true))

  @Provides
  open fun loginSmsListener(app: Application): LoginOtpSmsListener = LoginOtpSmsListenerImpl(app)
}
