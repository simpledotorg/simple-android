package org.simple.clinic.medicalhistory.newentry

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class NewMedicalHistoryScreenKey : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "New Medical History Entry"

  override fun layoutRes() = R.layout.screen_new_medical_history
}
