package org.simple.clinic.storage

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope

@Module
class SharedPreferencesModule {

  @Provides
  @AppScope
  fun rxSharedPreferences(preferences: SharedPreferences): RxSharedPreferences {
    return RxSharedPreferences.create(preferences)
  }

  @Provides
  @AppScope
  fun sharedPreferences(appContext: Application): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(appContext)
  }
}
