package org.simple.clinic.storage

import android.app.Application
import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppSqliteOpenHelperFactory

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
  open fun sqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory = AppSqliteOpenHelperFactory()
}
