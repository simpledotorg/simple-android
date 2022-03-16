package org.simple.clinic.appupdate.criticalupdatedialog

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.ContactType
import org.simple.clinic.appupdate.AppUpdateHelpContact
import java.util.Optional

class CriticalAppUpdateInitTest {

  private val defaultModel = CriticalAppUpdateModel.create()
  private val initSpec = InitSpec(CriticalAppUpdateInit())

  @Test
  fun `when screen is created, then load app update help contact and load app staleness`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadAppUpdateHelpContact, LoadAppStaleness)
        ))
  }

  @Test
  fun `when screen is restored, then don't load app update help contact`() {
    val appUpdateHelpContact = Optional.of(AppUpdateHelpContact(
        displayText = "+91 1111111111",
        url = "https://wa.me/911111111111/?text=I would like to ask a question about Simple",
        contactType = ContactType.WhatsApp
    ))
    val model = defaultModel.appUpdateHelpContactLoaded(appUpdateHelpContact)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadAppStaleness)
        ))
  }
}
