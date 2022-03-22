package org.simple.clinic.appupdate.criticalupdatedialog

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class CriticalAppUpdateDialogUpdate : Update<CriticalAppUpdateModel, CriticalAppUpdateEvent, CriticalAppUpdateEffect> {

  override fun update(model: CriticalAppUpdateModel, event: CriticalAppUpdateEvent): Next<CriticalAppUpdateModel, CriticalAppUpdateEffect> {
    return when (event) {
      is AppUpdateHelpContactLoaded -> next(model.appUpdateHelpContactLoaded(event.appUpdateHelpContact))
      ContactHelpClicked -> dispatch(OpenHelpContactUrl(model.contactUrl))
      UpdateAppClicked -> dispatch(OpenSimpleInGooglePlay)
      is AppStalenessLoaded -> noChange()
    }
  }
}
