package org.simple.clinic.main

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class TheActivityInit : Init<TheActivityModel, TheActivityEffect> {

  override fun init(model: TheActivityModel): First<TheActivityModel, TheActivityEffect> {
    return first(model, setOf(
        ListenForUserVerifications,
        ListenForUserUnauthorizations,
        ListenForUserDisapprovals,
        LoadInitialScreenInfo
    ))
  }
}
