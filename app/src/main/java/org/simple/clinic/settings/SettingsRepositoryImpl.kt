package org.simple.clinic.settings

import io.reactivex.Completable
import io.reactivex.Single

class SettingsRepositoryImpl : SettingsRepository {

  override fun getCurrentLanguage(): Single<Language> {
    return Single.never()
  }

  override fun getSupportedLanguages(): Single<List<Language>> {
    return Single.never()
  }

  override fun setCurrentLanguage(newLanguage: Language): Completable {
    return Completable.never()
  }
}
