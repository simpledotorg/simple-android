package org.simple.clinic.settings

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.LocalePreferenceConverter
import org.simple.clinic.util.preference.getOptional
import java.util.Locale
import javax.inject.Named

@Module
class SettingsModule {

  @Provides
  @Named("preference_user_selected_locale")
  fun provideUserSelectedLocalePreference(rxSharedPreferences: RxSharedPreferences): Preference<Optional<Locale>> {
    return rxSharedPreferences.getOptional("preference_user_selected_locale", LocalePreferenceConverter())
  }

  @Provides
  fun provideSettingsRepository(
      @Named("preference_user_selected_locale") userSelectedLocalePreference: Preference<Optional<Locale>>
  ): SettingsRepository {
    val supportedLanguages = listOf<Language>(
        ProvidedLanguage(displayName = "English", languageCode = "en-IN"),
        ProvidedLanguage(displayName = "हिंदी", languageCode = "hi-IN"),
        ProvidedLanguage(displayName = "ਪੰਜਾਬੀ", languageCode = "pa-IN"),
        ProvidedLanguage(displayName = "বাঙালি", languageCode = "bn-IN"),
        ProvidedLanguage(displayName = "ಕನ್ನಡ", languageCode = "kn-IN"),
        ProvidedLanguage(displayName = "मराठी", languageCode = "mr-IN"),
        ProvidedLanguage(displayName = "తెలుగు", languageCode = "te-IN"),
        ProvidedLanguage(displayName = "বাংলা", languageCode = "bn-BD"),
        ProvidedLanguage(displayName = "ትግርኛ", languageCode = "ti-ET"),
        ProvidedLanguage(displayName = "Afan Oromo", languageCode = "om-ET"),
        ProvidedLanguage(displayName = "አማርኛ", languageCode = "am-ET"),
        ProvidedLanguage(displayName = "தமிழ்", languageCode = "ta-IN"),
        ProvidedLanguage(displayName = "Soomaali", languageCode = "so-ET")
    )

    return PreferencesSettingsRepository(
        userSelectedLocalePreference = userSelectedLocalePreference,
        supportedLanguages = supportedLanguages
    )
  }
}
