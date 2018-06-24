package org.simple.clinic.login

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.user.LoggedInUserRxPreferencesConverter
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import retrofit2.Retrofit
import javax.inject.Named

@Module
class LoginModule {

  @Provides
  fun loginApi(retrofit: Retrofit): LoginApiV1 {
    return retrofit.create(LoginApiV1::class.java)
  }

  @Provides
  fun loggedInUser(rxSharedPrefs: RxSharedPreferences, rxPrefsConverter: LoggedInUserRxPreferencesConverter): Preference<Optional<LoggedInUser>> {
    return rxSharedPrefs.getObject("logged_in_user", None, OptionalRxPreferencesConverter(rxPrefsConverter))
  }

  @Provides
  @Named("jsonadapter_loggedinuser")
  fun loggedInUserJsonAdapter(moshi: Moshi): JsonAdapter<LoggedInUser> {
    return moshi.adapter(LoggedInUser::class.java)
  }

  @Provides
  fun loggedInUserRxPrefsConverter(@Named("jsonadapter_loggedinuser") adapter: JsonAdapter<LoggedInUser>): LoggedInUserRxPreferencesConverter {
    return LoggedInUserRxPreferencesConverter(adapter)
  }
}
