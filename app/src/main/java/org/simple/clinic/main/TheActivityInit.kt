package org.simple.clinic.main

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class TheActivityInit : Init<TheActivityModel, TheActivityEffect> {

  override fun init(model: TheActivityModel): First<TheActivityModel, TheActivityEffect> {
    val effects = mutableSetOf(
        ListenForUserVerifications,
        ListenForUserUnauthorizations,
        ListenForUserDisapprovals
    )

    if (!model.isFreshLogin) {
      effects.add(LoadAppLockInfo)
    }

    return first(model, effects)
  }
}
