package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class OverdueScreenKey : FullScreenKey, Parcelable {

  override fun layoutRes() = R.layout.screen_overdue
}
