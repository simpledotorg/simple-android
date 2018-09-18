package org.simple.clinic.search

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
@Suppress("EqualsOrHashCode")
class PatientSearchScreenKey(
    val preFilledName: String = "",
    val preFilledAge: String = "",
    val preFilledDateOfBirth: String = ""
) : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName = "Patient Search"

  override fun layoutRes(): Int {
    return R.layout.screen_patient_search
  }

  // Flow decides the launchMode of screens by using equals().
  // There should only exist one search screen regardless of
  // the constructor params.
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PatientSearchScreenKey) return false
    return true
  }
}
