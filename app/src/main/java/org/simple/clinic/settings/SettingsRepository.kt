package org.simple.clinic.settings

import io.reactivex.Completable
import io.reactivex.Single

interface SettingsRepository {

  fun getCurrentLanguage(): Single<Language>

  fun getSupportedLanguages(): Single<List<Language>>

  fun setCurrentLanguage(newLanguage: Language): Completable
}
