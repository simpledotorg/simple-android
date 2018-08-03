package org.simple.clinic.di

import android.app.Application
import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory

@Module
open class StorageModule {

  @Provides
  fun rxSharedPreferences(appContext: Application): RxSharedPreferences {
    val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
    return RxSharedPreferences.create(preferences)
  }

  @Provides
  fun sharedPreferences(appContext: Application): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(appContext)
  }

  @Provides
  fun sqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory = RequerySQLiteOpenHelperFactory()
}
