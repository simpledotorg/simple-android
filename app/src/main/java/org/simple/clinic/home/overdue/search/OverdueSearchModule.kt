package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader

@Module
object OverdueSearchModule {

  @Provides
  fun overdueSearchHistory(
      rxSharedPreferences: RxSharedPreferences
  ): Preference<Set<String>> {
    return rxSharedPreferences.getStringSet("preference_overdue_search_history_v1")
  }

  @Provides
  fun overdueSearchConfig(
      configReader: ConfigReader
  ): OverdueSearchConfig {
    return OverdueSearchConfig.read(configReader)
  }
}
