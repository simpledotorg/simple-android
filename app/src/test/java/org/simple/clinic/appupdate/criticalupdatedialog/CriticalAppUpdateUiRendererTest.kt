package org.simple.clinic.appupdate.criticalupdatedialog

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.ContactType
import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL_SECURITY
import java.time.LocalDate
import java.util.Optional

class CriticalAppUpdateUiRendererTest {

  private val ui = mock<CriticalAppUpdateUi>()
  private val uiRenderer = CriticalAppUpdateUiRenderer(ui, LocalDate.of(2022, 3, 22))
  private val defaultModel = CriticalAppUpdateModel.create(CRITICAL)

  @Test
  fun `when help contact is available, then show the help section and contact support phone number`() {
    // given
    val supportContact = "+91 1111111111"
    val contactType = ContactType.WhatsApp
    val appUpdateHelpContact = Optional.of(AppUpdateHelpContact(
        displayText = supportContact,
        url = "https://wa.me/911111111111/?text=I would like to ask a question about Simple",
        contactType = contactType
    ))
    val model = defaultModel.appUpdateHelpContactLoaded(appUpdateHelpContact)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showHelp()
    verify(ui).showSupportContactPhoneNumber(supportContact, contactType)
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

  @Test
  fun `when app staleness is available and nudge priority is critical, then render critical app update reason`() {
    // given
    val appStaleness = 75

    // when
    uiRenderer.render(defaultModel.appStalenessLoaded(appStaleness))

    // then
    verify(ui).hideHelp()
    verify(ui).renderCriticalAppUpdateReason(appStalenessInMonths = 2)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when nudge priority is critical security, then render critical security app update reason`() {
    // given
    val criticalSecurityModel = CriticalAppUpdateModel.create(CRITICAL_SECURITY)

    // when
    uiRenderer.render(criticalSecurityModel)

    // then
    verify(ui).hideHelp()
    verify(ui).renderCriticalSecurityAppUpdateReason()
    verifyNoMoreInteractions(ui)
  }
}
