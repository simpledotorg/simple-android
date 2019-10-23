package org.simple.clinic.setup

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class SetupActivityInit : Init<SetupActivityModel, SetupActivityEffect> {

  override fun init(model: SetupActivityModel): First<SetupActivityModel, SetupActivityEffect> {
    return first(model, setOf(FetchUserDetails))
  }
}
