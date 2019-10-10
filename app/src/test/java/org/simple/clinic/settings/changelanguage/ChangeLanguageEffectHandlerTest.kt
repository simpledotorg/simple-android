package org.simple.clinic.settings.changelanguage

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.After
import org.junit.Test
import org.simple.clinic.settings.ProvidedLanguage
import org.simple.clinic.settings.SettingsRepository
import org.simple.clinic.settings.SystemDefaultLanguage
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class ChangeLanguageEffectHandlerTest {

  private val settingsRepository = mock<SettingsRepository>()
  private val uiActions = mock<UiActions>()

  private val effectsSubject: Subject<ChangeLanguageEffect> = PublishSubject.create<ChangeLanguageEffect>()

  private val testObserver: TestObserver<ChangeLanguageEvent> = effectsSubject
      .compose(ChangeLanguageEffectHandler.create(
          schedulersProvider = TrampolineSchedulersProvider(),
          settingsRepository = settingsRepository,
          uiActions = uiActions
      ))
      .test()

  @After
  fun tearDown() {
    testObserver.dispose()
  }

  @Test
  fun `the current selected language must be fetched when the load current selected effect is received`() {
    // given
    val selectedLanguage = SystemDefaultLanguage
    whenever(settingsRepository.getCurrentLanguage()).thenReturn(Single.just(selectedLanguage))

    // when
    effectsSubject.onNext(LoadCurrentLanguageEffect)

    // then
    testObserver
        .assertValue(CurrentLanguageLoadedEvent(selectedLanguage))
        .assertNotComplete()
        .assertNotTerminated()
  }

  @Test
  fun `the list of supported languages must be fetched when the load supported languages effect is received`() {
    // given
    val supportedLanguages = listOf(
        SystemDefaultLanguage,
        ProvidedLanguage(displayName = "English", languageCode = "en_IN"),
        ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
    )
    whenever(settingsRepository.getSupportedLanguages()).thenReturn(Single.just(supportedLanguages))

    // when
    effectsSubject.onNext(LoadSupportedLanguagesEffect)

    // then
    testObserver
        .assertValue(SupportedLanguagesLoadedEvent(supportedLanguages))
        .assertNotComplete()
        .assertNotTerminated()
  }

  @Test
  fun `when the update current language effect is received, the current language must be changed`() {
    // given
    val changeToLanguage = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
    whenever(settingsRepository.setCurrentLanguage(changeToLanguage)).thenReturn(Completable.complete())

    // when
    effectsSubject.onNext(UpdateCurrentLanguageEffect(changeToLanguage))

    // then
    testObserver
        .assertValue(CurrentLanguageChangedEvent)
        .assertNotComplete()
        .assertNotTerminated()
  }

  @Test
  fun `when the go back to previous screen effect is received, the go back ui action must be invoked`() {
    // when
    effectsSubject.onNext(GoBack)

    // then
    testObserver
        .assertNoValues()
        .assertNotComplete()
        .assertNotTerminated()
    verify(uiActions).goBackToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
