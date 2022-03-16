package org.simple.clinic.appupdate.criticalupdatedialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.ContactType
import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import java.util.Optional

class CriticalAppUpdateUiRendererTest {

  private val ui = mock<CriticalAppUpdateUi>()
  private val uiRenderer = CriticalAppUpdateUiRenderer(ui)
  private val defaultModel = CriticalAppUpdateModel.create(CRITICAL)

  @Test
  fun `when help contact is available, then show the help section`() {
    // given
    val appUpdateHelpContact = Optional.of(AppUpdateHelpContact(
        displayText = "+91 1111111111",
        url = "https://wa.me/911111111111/?text=I would like to ask a question about Simple",
        contactType = ContactType.WhatsApp
    ))
    val model = defaultModel.appUpdateHelpContactLoaded(appUpdateHelpContact)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showHelp()
    verifyNoMoreInteractions(ui)
  }
  
  @Test
  fun `when help contact is not available, then hide the help section`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).hideHelp()
    verifyNoMoreInteractions(ui)
  }
}
