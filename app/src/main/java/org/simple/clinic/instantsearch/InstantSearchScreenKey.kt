package org.simple.clinic.instantsearch

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.PatientPrefillInfo

@Parcelize
data class InstantSearchScreenKey(
    val additionalIdentifier: Identifier?,
    val initialSearchQuery: String?,
    val patientPrefillInfo: PatientPrefillInfo?
) : ScreenKey(), Parcelable {

  @IgnoredOnParcel
  override val analyticsName: String = "Instant Search Screen"

  override fun instantiateFragment() = InstantSearchScreen()
}
