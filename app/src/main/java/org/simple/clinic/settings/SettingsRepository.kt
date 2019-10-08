package org.simple.clinic.settings

import io.reactivex.Completable
import io.reactivex.Single

interface SettingsRepository {

  fun getCurrentSelectedLanguage(): Single<Language>

  fun getSupportedLanguages(): Single<List<Language>>

  fun setCurrentSelectedLanguage(newLanguage: Language): Completable
}
