package org.simple.clinic.main

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class TheActivityUpdate : Update<TheActivityModel, TheActivityEvent, TheActivityEffect> {

  override fun update(model: TheActivityModel, event: TheActivityEvent): Next<TheActivityModel, TheActivityEffect> {
    return when(event) {
      LifecycleEvent.ActivityStarted -> noChange()
      LifecycleEvent.ActivityStopped -> noChange()
      LifecycleEvent.ActivityDestroyed -> noChange()
      is AppLockInfoLoaded -> noChange()
    }
  }
}
