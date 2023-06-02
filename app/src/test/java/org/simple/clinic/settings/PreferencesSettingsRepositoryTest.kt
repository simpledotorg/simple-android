package org.simple.clinic.settings

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test
import java.util.Locale
import java.util.Optional

class PreferencesSettingsRepositoryTest {

  private val preference = mock<Preference<Optional<Locale>>>()

  private val english = ProvidedLanguage(displayName = "English", languageCode = "en-IN")
  private val kannada = ProvidedLanguage(displayName = "ಕನ್ನಡ", languageCode = "kn-IN")

  private val repository = PreferencesSettingsRepository(preference, listOf(english, kannada))

  @Test
  fun `if user selected locale is not set, fetching the current language should return the default language`() {
    // given
    whenever(preference.get()).doReturn(Optional.empty())

    // then
    repository
        .getCurrentLanguage()
        .test()
        .assertValue(SystemDefaultLanguage)
  }

  @Test
  fun `if user selected locale is in the list of provided languages, fetching the current language should return the provided language`() {
    // given
    val locale = kannada.toLocale()
    whenever(preference.get()).doReturn(Optional.of(locale))

    // then
    repository
        .getCurrentLanguage()
        .test()
        .assertValue(kannada)
  }

  @Test
  fun `if user selected locale is not in the list of provided languages, fetching the current language should return the default language`() {
    // given
    val locale = Locale.forLanguageTag("hi-IN")
    whenever(preference.get()).doReturn(Optional.of(locale))

    // then
    repository
        .getCurrentLanguage()
        .test()
        .assertValue(SystemDefaultLanguage)
  }

  @Test
  fun `when setting the current language to a provided language, the user selected locale preference must be set`() {
    // given
    val locale = kannada.toLocale()

    // when
    repository.setCurrentLanguage(kannada).blockingAwait()

    // then
    verify(preference).set(Optional.of(locale))
  }
}
