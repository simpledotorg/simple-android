package org.simple.clinic.login.applock

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class AppLockUpdate : Update<AppLockModel, AppLockEvent, AppLockEffect> {

  override fun update(model: AppLockModel, event: AppLockEvent): Next<AppLockModel, AppLockEffect> {
    return when (event) {
      AppLockBackClicked -> dispatch(ExitApp)
    }
  }
}
