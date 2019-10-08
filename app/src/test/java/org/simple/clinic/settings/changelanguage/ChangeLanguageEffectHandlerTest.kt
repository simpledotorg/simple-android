package org.simple.clinic.settings.changelanguage

import com.nhaarman.mockito_kotlin.mock
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

  private val effectsSubject: Subject<ChangeLanguageEffect> = PublishSubject.create<ChangeLanguageEffect>()

  private val testObserver: TestObserver<ChangeLanguageEvent> = effectsSubject
      .compose(ChangeLanguageEffectHandler.create(
          schedulersProvider = TrampolineSchedulersProvider(),
          settingsRepository = settingsRepository
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
    whenever(settingsRepository.getCurrentSelectedLanguage()).thenReturn(Single.just(selectedLanguage))

    // when
    effectsSubject.onNext(LoadCurrentSelectedLanguageEffect)

    // then
    testObserver
        .assertValue(CurrentSelectedLanguageLoadedEvent(selectedLanguage))
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
  fun `when the update selected language effect is received, the selected language must be changed`() {
    // given
    val changeToLanguage = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
    whenever(settingsRepository.setCurrentSelectedLanguage(changeToLanguage)).thenReturn(Completable.complete())

    // when
    effectsSubject.onNext(UpdateSelectedLanguageEffect(changeToLanguage))

    // then
    testObserver
        .assertValue(SelectedLanguageChangedEvent(changeToLanguage))
        .assertNotComplete()
        .assertNotTerminated()
  }
}
