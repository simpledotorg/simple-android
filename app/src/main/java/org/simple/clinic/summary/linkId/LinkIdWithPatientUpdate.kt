package org.simple.clinic.summary.linkId

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class LinkIdWithPatientUpdate : Update<LinkIdWithPatientModel, LinkIdWithPatientEvent, LinkIdWithPatientEffect> {

  override fun update(model: LinkIdWithPatientModel, event: LinkIdWithPatientEvent): Next<LinkIdWithPatientModel, LinkIdWithPatientEffect> {
    return noChange()
  }
}
