package org.simple.clinic.settings

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import java.util.Locale

class SettingsRepositoryImplTest {

  private val preference = mock<Preference<Optional<Locale>>>()

  private val english = ProvidedLanguage(displayName = "English", languageCode = "en-IN")
  private val kannada = ProvidedLanguage(displayName = "ಕನ್ನಡ", languageCode = "kn-IN")

  private val repository = SettingsRepositoryImpl(preference, listOf(english, kannada))

  @Test
  fun `if user selected locale is not set, fetching the current language should return the default language`() {
    // given
    whenever(preference.get()).doReturn(None)

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
    whenever(preference.get()).doReturn(Just(locale))

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
    whenever(preference.get()).doReturn(Just(locale))

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
    verify(preference).set(Just(locale))
  }
}
