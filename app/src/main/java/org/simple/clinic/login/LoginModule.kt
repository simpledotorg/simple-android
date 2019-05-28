package org.simple.clinic.login

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.security.BCryptPasswordHasher
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.simple.clinic.util.StringPreferenceConverter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Module
open class LoginModule {

  @Provides
  fun loginApi(retrofit: Retrofit): LoginApi {
    return retrofit.create(LoginApi::class.java)
  }

  @Provides
  @Named("preference_access_token")
  fun accessTokenForLoggedInUser(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("access_token", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
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
  open fun loginSmsListener(app: Application): LoginOtpSmsListener = LoginOtpSmsListenerImpl(app)

  @Provides
  fun ongoingLoginEntryDao(appDatabase: AppDatabase): OngoingLoginEntry.RoomDao {
    return appDatabase.ongoingLoginEntryDao()
  }
}
