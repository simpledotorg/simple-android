package org.simple.clinic.settings.changelanguage

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.settings.Language
import org.simple.clinic.settings.ProvidedLanguage

class ChangeLanguageListItemTest {

  private val englishIndia = ProvidedLanguage(displayName = "English", languageCode = "en_IN")
  private val hindiIndia = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")

  @Test
  fun `list items must be generated when no language has been selected`() {
    // given
    val supportedLanguages: List<Language> = listOf(englishIndia, hindiIndia)

    // when
    val listItems = ChangeLanguageListItem.from(supportedLanguages, null)

    // then
    assertThat(listItems)
        .containsExactly(
            ChangeLanguageListItem(language = englishIndia, isSelected = false),
            ChangeLanguageListItem(language = hindiIndia, isSelected = false)
        )
        .inOrder()
  }

  @Test
  fun `list items must be generated when the selected language is present in the supported languages`() {
    // given
    val supportedLanguages: List<Language> = listOf(englishIndia, hindiIndia)

    // when
    val listItems = ChangeLanguageListItem.from(supportedLanguages, hindiIndia)

    // then
    assertThat(listItems)
        .containsExactly(
            ChangeLanguageListItem(language = englishIndia, isSelected = false),
            ChangeLanguageListItem(language = hindiIndia, isSelected = true)
        )
        .inOrder()
  }

  @Test
  fun `list items must be generated when the selected language is not present in the supported languages`() {
    // given
    val tamilIndia = ProvidedLanguage(displayName = "தமிழ்", languageCode = "ta_IN")
    val supportedLanguages: List<Language> = listOf(englishIndia, hindiIndia)

    // when
    val listItems = ChangeLanguageListItem.from(supportedLanguages, tamilIndia)

    // then
    assertThat(listItems)
        .containsExactly(
            ChangeLanguageListItem(language = englishIndia, isSelected = false),
            ChangeLanguageListItem(language = hindiIndia, isSelected = false)
        )
        .inOrder()
  }
}
