package org.simple.clinic.appupdate.criticalupdatedialog

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.ContactType.WhatsApp
import org.simple.clinic.appupdate.AppUpdateHelpContact
import java.util.Optional

class CriticalAppUpdateDialogUpdateTest {

  private val defaultModel = CriticalAppUpdateModel.create()
  private val updateSpec = UpdateSpec(CriticalAppUpdateDialogUpdate())

  @Test
  fun `when app update help contact is loaded, then update the model`() {
    val appUpdateHelpContact = Optional.of(AppUpdateHelpContact(
        displayText = "+91 1111111111",
        url = "https://wa.me/911111111111/?text=I would like to ask a question about Simple",
        contactType = WhatsApp
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(AppUpdateHelpContactLoaded(appUpdateHelpContact))
        .then(assertThatNext(
            hasModel(defaultModel.appUpdateHelpContactLoaded(appUpdateHelpContact)),
            hasNoEffects()
        ))
  }
}
