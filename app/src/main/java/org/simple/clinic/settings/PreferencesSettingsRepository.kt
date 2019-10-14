package org.simple.clinic.settings

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.ofType
import java.util.Locale

class PreferencesSettingsRepository(
    private val userSelectedLocalePreference: Preference<Optional<Locale>>,
    private val supportedLanguages: List<Language>
) : SettingsRepository {

  override fun getCurrentLanguage(): Single<Language> {
    return Single.just(userSelectedLocalePreference.get())
        .filterAndUnwrapJust()
        .flatMap(::findSupportedLanguageForLocale)
        .toSingle(SystemDefaultLanguage)
  }

  private fun findSupportedLanguageForLocale(locale: Locale): Maybe<Language> {
    return Maybe.fromCallable {
      supportedLanguages
          .filterIsInstance<ProvidedLanguage>()
          .find { it.matchesLocale(locale) }
    }
  }

  override fun getSupportedLanguages(): Single<List<Language>> {
    return Single.just(supportedLanguages)
  }

  override fun setCurrentLanguage(newLanguage: Language): Completable {
    return Single.just(newLanguage)
        .ofType<ProvidedLanguage>()
        .map(ProvidedLanguage::toLocale)
        .flatMapCompletable { localeToSet ->
          Completable.fromAction { userSelectedLocalePreference.set(Just(localeToSet)) }
        }
  }
}
