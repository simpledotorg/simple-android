package org.simple.clinic.settings.changelanguage

import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.junit.After
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class ChangeLanguageEffectHandlerTest {

  private val effectsSubject: Subject<ChangeLanguageEffect> = PublishSubject.create<ChangeLanguageEffect>()

  private val testObserver: TestObserver<ChangeLanguageEvent> = effectsSubject
      .compose(ChangeLanguageEffectHandler.create(TrampolineSchedulersProvider()))
      .test()

  @After
  fun tearDown() {
    testObserver.dispose()
  }
}
