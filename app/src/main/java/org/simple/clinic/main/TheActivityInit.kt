package org.simple.clinic.main

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class TheActivityInit : Init<TheActivityModel, TheActivityEffect> {

  override fun init(model: TheActivityModel): First<TheActivityModel, TheActivityEffect> {
    return first(
        model,
        ListenForUserVerifications,
        ListenForUserUnauthorizations,
        ListenForUserDisapprovals,
        LoadAppLockInfo
    )
  }
}
