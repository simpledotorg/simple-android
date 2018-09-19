package org.simple.clinic.login

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.forgotpin.ForgotPinApiV1
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.login.applock.BCryptPasswordHasher
import org.simple.clinic.login.applock.PasswordHasher
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

  @Provides
  fun forgotPinApi(retrofit: Retrofit): ForgotPinApiV1 {
    return object : ForgotPinApiV1 {}
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
