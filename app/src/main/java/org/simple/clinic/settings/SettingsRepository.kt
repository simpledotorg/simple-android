package org.simple.clinic.settings

import io.reactivex.Completable
import io.reactivex.Single

class SettingsRepository {

  fun getCurrentSelectedLanguage(): Single<Language> {
    TODO()
  }

  fun getSupportedLanguages(): Single<List<Language>> {
    TODO()
  }

  fun setCurrentSelectedLanguage(newLanguage: Language): Completable {
    TODO()
  }
}
