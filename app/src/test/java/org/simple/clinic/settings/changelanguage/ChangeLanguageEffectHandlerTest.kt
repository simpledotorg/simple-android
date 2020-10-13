package org.simple.clinic.settings.changelanguage

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.settings.Language
import org.simple.clinic.settings.ProvidedLanguage
import org.simple.clinic.settings.SettingsRepository
import org.simple.clinic.settings.SystemDefaultLanguage
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncTag
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class ChangeLanguageEffectHandlerTest {

  private val settingsRepository = mock<SettingsRepository>()
  private val uiActions = mock<UiActions>()

  private val dataSync = mock<DataSync>()
  private val effectHandler = ChangeLanguageEffectHandler(
      schedulersProvider = TrampolineSchedulersProvider(),
      settingsRepository = settingsRepository,
      uiActions = uiActions,
      dataSync = dataSync
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `the current selected language must be fetched when the load current selected effect is received`() {
    // given
    val selectedLanguage = SystemDefaultLanguage
    whenever(settingsRepository.getCurrentLanguage()).doReturn(Single.just<Language>(selectedLanguage))

    // when
    testCase.dispatch(LoadCurrentLanguageEffect)

    // then
    testCase.assertOutgoingEvents(CurrentLanguageLoadedEvent(selectedLanguage))
  }

  @Test
  fun `the list of supported languages must be fetched when the load supported languages effect is received`() {
    // given
    val supportedLanguages = listOf(
        SystemDefaultLanguage,
        ProvidedLanguage(displayName = "English", languageCode = "en_IN"),
        ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
    )
    whenever(settingsRepository.getSupportedLanguages()).doReturn(Single.just(supportedLanguages))

    // when
    testCase.dispatch(LoadSupportedLanguagesEffect)

    // then
    testCase.assertOutgoingEvents(SupportedLanguagesLoadedEvent(supportedLanguages))
  }

  @Test
  fun `when the update current language effect is received, the current language must be changed`() {
    // given
    val changeToLanguage = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
    whenever(settingsRepository.setCurrentLanguage(changeToLanguage)).doReturn(Completable.complete())

    // when
    testCase.dispatch(UpdateCurrentLanguageEffect(changeToLanguage))

    // then
    testCase.assertOutgoingEvents(CurrentLanguageChangedEvent)
  }

  @Test
  fun `when the go back to previous screen effect is received, the go back ui action must be invoked`() {
    // when
    testCase.dispatch(GoBack)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goBackToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the restart activity effect is received, the restart activity ui action must be invoked`() {
    // when
    testCase.dispatch(RestartActivity)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).restartActivity()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when trigger sync effect is received, then sync data`() {
    // when
    testCase.dispatch(TriggerSync)

    // then
    testCase.assertNoOutgoingEvents()
    verify(dataSync).fireAndForgetSync(SyncTag.DAILY)
    verifyZeroInteractions(uiActions)
  }

}
