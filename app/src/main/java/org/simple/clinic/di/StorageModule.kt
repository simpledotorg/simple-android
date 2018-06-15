package org.simple.clinic.di

import android.app.Application
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides

@Module
class StorageModule {

  @Provides
  fun rxSharedPreferences(appContext: Application): RxSharedPreferences {
    val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
    return RxSharedPreferences.create(preferences)
  }
}
