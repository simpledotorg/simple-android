package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.OverdueSearchHistory
import org.simple.clinic.remoteconfig.ConfigReader

@Module
object OverdueSearchModule {

  @Provides
  @TypedPreference(OverdueSearchHistory)
  fun overdueSearchHistoryPreference(
      rxSharedPreferences: RxSharedPreferences
  ): Preference<String> {
    return rxSharedPreferences.getString("preference_overdue_search_history_v1")
  }

  @Provides
  fun overdueSearchConfig(
      configReader: ConfigReader
  ): OverdueSearchConfig {
    return OverdueSearchConfig.read(configReader)
  }
}
