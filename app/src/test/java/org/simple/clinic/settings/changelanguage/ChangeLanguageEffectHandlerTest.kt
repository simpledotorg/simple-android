package org.simple.clinic.settings.changelanguage

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.After
import org.junit.Test
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
}
