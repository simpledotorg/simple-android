package org.simple.clinic.appupdate.criticalupdatedialog

import org.junit.After
import org.junit.Test
import org.simple.clinic.ContactType.WhatsApp
import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.Optional

class CriticalAppUpdateEffectHandlerTest {

  private val appUpdateHelpContact = Optional.of(AppUpdateHelpContact(
      displayText = "+91 1111111111",
      url = "https://wa.me/911111111111/?text=I would like to ask a question about Simple",
      contactType = WhatsApp
  ))
  private val effectHandler = CriticalAppUpdateEffectHandler(
      appUpdateHelpContact = { appUpdateHelpContact },
      schedulersProvider = TestSchedulersProvider.trampoline()
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load app update help contact effect is received, then load app update help contact`() {
    // when
    testCase.dispatch(LoadAppUpdateHelpContact)

    // then
    testCase.assertOutgoingEvents(AppUpdateHelpContactLoaded(appUpdateHelpContact))
  }
}
