package org.simple.clinic.facility.alertchange

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEffect.LoadIsFacilityChangedStatus
import org.simple.clinic.mobius.first

class AlertFacilityChangeInit : Init<AlertFacilityChangeModel, AlertFacilityChangeEffect> {

  override fun init(model: AlertFacilityChangeModel): First<AlertFacilityChangeModel, AlertFacilityChangeEffect> {
    return first(model, LoadIsFacilityChangedStatus)
  }
}
