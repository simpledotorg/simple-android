package org.simple.clinic.summary.linkId

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class LinkIdWithPatientInit : Init<LinkIdWithPatientModel, LinkIdWithPatientEffect> {
  override fun init(model: LinkIdWithPatientModel): First<LinkIdWithPatientModel, LinkIdWithPatientEffect> {
    val effects = mutableSetOf<LinkIdWithPatientEffect>()

    if (!model.hasPatientName) {
      effects.add(GetPatientNameFromId(model.patientUuid))
    }

    return first(model, effects)
  }
}
