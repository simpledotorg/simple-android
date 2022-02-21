package org.simple.clinic.appupdate.criticalupdatedialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.ContactType.WhatsApp
import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.Optional

class CriticalAppUpdateEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val appUpdateHelpContact = Optional.of(AppUpdateHelpContact(
      displayText = "+91 1111111111",
      url = "https://wa.me/911111111111/?text=I would like to ask a question about Simple",
      contactType = WhatsApp
  ))
  private val effectHandler = CriticalAppUpdateEffectHandler(
      appUpdateHelpContact = { appUpdateHelpContact },
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = CriticalAppUpdateViewEffectHandler(uiActions)::handle
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
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open help contact effect is received, then open the contact url`() {
    // given
    val contactUrl = "https://wa.me/911111111111/?text=I would like to ask a question about Simple"

    // when
    testCase.dispatch(OpenHelpContactUrl(contactUrl))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openContactUrl(contactUrl)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open simple in google play effect is received, then open simple in google play`() {
    // when
    testCase.dispatch(OpenSimpleInGooglePlay)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openSimpleInGooglePlay()
    verifyNoMoreInteractions(uiActions)
  }
}
