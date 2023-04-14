package org.simple.clinic.settings

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.preference.LocalePreferenceConverter
import org.simple.clinic.util.preference.getOptional
import java.util.Locale
import java.util.Optional
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
        ProvidedLanguage(displayName = "Afan Oromo", languageCode = "om-ET"),
        ProvidedLanguage(displayName = "አማርኛ", languageCode = "am-ET"),
        ProvidedLanguage(displayName = "বাংলা", languageCode = "bn-BD"),
        ProvidedLanguage(displayName = "বাঙালি", languageCode = "bn-IN"),
        ProvidedLanguage(displayName = "English", languageCode = "en-IN"),
        ProvidedLanguage(displayName = "Español", languageCode = "es"),
        ProvidedLanguage(displayName = "हिंदी", languageCode = "hi-IN"),
        ProvidedLanguage(displayName = "ಕನ್ನಡ", languageCode = "kn-IN"),
        ProvidedLanguage(displayName = "मराठी", languageCode = "mr-IN"),
        ProvidedLanguage(displayName = "ਪੰਜਾਬੀ", languageCode = "pa-IN"),
        ProvidedLanguage(displayName = "Soomaali", languageCode = "so-ET"),
        ProvidedLanguage(displayName = "Sidama", languageCode = "sid-ET"),
        ProvidedLanguage(displayName = "සිංහල", languageCode = "si-LK"),
        ProvidedLanguage(displayName = "తెలుగు", languageCode = "te-IN"),
        ProvidedLanguage(displayName = "ትግርኛ", languageCode = "ti-ET"),
        ProvidedLanguage(displayName = "தமிழ் (இந்தியா)", languageCode = "ta-IN"),
        ProvidedLanguage(displayName = "தமிழ் (இலங்கை)", languageCode = "ta-LK"),
    )

    return PreferencesSettingsRepository(
        userSelectedLocalePreference = userSelectedLocalePreference,
        supportedLanguages = supportedLanguages
    )
  }
}
