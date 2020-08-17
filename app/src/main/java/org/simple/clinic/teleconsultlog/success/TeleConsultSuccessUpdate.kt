package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class TeleConsultSuccessUpdate : Update<TeleConsultSuccessModel, TeleConsultSuccessEvent, TeleConsultSuccessEffect> {
  override fun update(model: TeleConsultSuccessModel, event: TeleConsultSuccessEvent): Next<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
    return Next.noChange()
  }
}
