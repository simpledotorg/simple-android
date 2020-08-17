package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class TeleConsultSuccessInit : Init<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
  override fun init(model: TeleConsultSuccessModel): First<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
    return first(model)
  }
}
