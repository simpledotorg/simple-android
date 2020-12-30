package org.simple.clinic.instantsearch

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class InstantSearchScreenKey(
    val additionalIdentifier: Identifier?
) : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName: String = "Instant Search Screen"

  override fun layoutRes(): Int {
    return R.layout.screen_instant_search
  }
}
