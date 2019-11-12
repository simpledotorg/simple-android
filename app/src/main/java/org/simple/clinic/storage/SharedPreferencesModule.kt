package org.simple.clinic.storage

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides

@Module
class SharedPreferencesModule {

  @Provides
  fun rxSharedPreferences(preferences: SharedPreferences): RxSharedPreferences {
    return RxSharedPreferences.create(preferences)
  }

  @Provides
  fun sharedPreferences(appContext: Application): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(appContext)
  }
}
