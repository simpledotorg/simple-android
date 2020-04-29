package org.simple.clinic.addidtopatient.searchforpatient

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class AddIdToPatientSearchScreenKey(val identifier: Identifier) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Add ID To Patient:Search"

  override fun layoutRes() = R.layout.screen_addidtopatientsearch
}
