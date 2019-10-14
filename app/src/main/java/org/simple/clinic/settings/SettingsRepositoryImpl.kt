package org.simple.clinic.settings

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.simple.clinic.util.Optional
import org.simple.clinic.util.filterAndUnwrapJust
import java.util.Locale

class SettingsRepositoryImpl(
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
    return Single.never()
  }

  override fun setCurrentLanguage(newLanguage: Language): Completable {
    return Completable.never()
  }
}
