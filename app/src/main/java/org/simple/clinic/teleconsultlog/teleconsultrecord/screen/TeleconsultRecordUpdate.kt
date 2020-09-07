package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class TeleconsultRecordUpdate : Update<TeleconsultRecordModel, TeleconsultRecordEvent, TeleconsultRecordEffect> {

  override fun update(model: TeleconsultRecordModel, event: TeleconsultRecordEvent): Next<TeleconsultRecordModel, TeleconsultRecordEffect> {
    return when (event) {
      BackClicked -> dispatch(GoBack)
      is TeleconsultRecordWithPrescribedDrugsLoaded -> noChange()
      TeleconsultRecordCreated -> noChange()
    }
  }
}
