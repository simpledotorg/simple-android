package org.simple.clinic.login.applock

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class AppLockInit : Init<AppLockModel, AppLockEffect> {

  override fun init(model: AppLockModel): First<AppLockModel, AppLockEffect> {
    return first(model)
  }
}
