package org.simple.clinic.login

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.security.BCryptPasswordHasher
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.OptionalRxPreferencesConverter
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.scheduler.SchedulersProvider
import retrofit2.Retrofit
import javax.inject.Named

@Module(includes = [AppLockConfigModule::class])
open class LoginModule {

  @Provides
  fun loginApi(@Named("for_country") retrofit: Retrofit): LoginApi {
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
  open fun loginSmsListener(app: Application, schedulersProvider: SchedulersProvider): LoginOtpSmsListener {
    return LoginOtpSmsListenerImpl(app, schedulersProvider)
  }

  @Provides
  fun ongoingLoginEntryDao(appDatabase: AppDatabase): OngoingLoginEntry.RoomDao {
    return appDatabase.ongoingLoginEntryDao()
  }
}
