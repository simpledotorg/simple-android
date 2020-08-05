package org.simple.clinic.home.help

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HelpScreenModel(
    val helpContent: String?
) : Parcelable {

  companion object {
    fun create() = HelpScreenModel(
        helpContent = null
    )
  }

  val hasHelpContent: Boolean
    get() = helpContent != null

  fun helpContentLoaded(helpContent: String?): HelpScreenModel {
    return copy(helpContent = helpContent)
  }
}
