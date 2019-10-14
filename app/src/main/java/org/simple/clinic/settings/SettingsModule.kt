package org.simple.clinic.settings

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.LocalePreferenceConverter
import org.simple.clinic.util.preference.OptionalRxPreferencesConverter
import java.util.Locale
import javax.inject.Named

@Module
class SettingsModule {

  @Provides
  @Named("preference_user_selected_locale")
  fun provideUserSelectedLocalePreference(rxSharedPreferences: RxSharedPreferences): Preference<Optional<Locale>> {
    return rxSharedPreferences.getObject("preference_user_selected_locale", None, OptionalRxPreferencesConverter(LocalePreferenceConverter()))
  }

  @Provides
  fun provideSettingsRepository(
      @Named("preference_user_selected_locale") userSelectedLocalePreference: Preference<Optional<Locale>>
  ): SettingsRepository {
    val supportedLanguages = listOf<Language>(
        ProvidedLanguage(displayName = "English", languageCode = "en-IN"),
        ProvidedLanguage(displayName = "हिंदी", languageCode = "hi-IN"),
        ProvidedLanguage(displayName = "ਪੰਜਾਬੀ", languageCode = "pa-IN")
    )

    return PreferencesSettingsRepository(
        userSelectedLocalePreference = userSelectedLocalePreference,
        supportedLanguages = supportedLanguages
    )
  }
}
