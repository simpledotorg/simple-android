package org.simple.clinic.login.applock

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class AppLockUpdate : Update<AppLockModel, AppLockEvent, AppLockEffect> {

  override fun update(model: AppLockModel, event: AppLockEvent): Next<AppLockModel, AppLockEffect> {
    return noChange()
  }
}
