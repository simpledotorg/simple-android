package org.simple.clinic.home.help

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.help.HelpPullResult

@Parcelize
data class HelpScreenModel(
    val helpContent: String?,
    val helpPullResult: HelpPullResult?
) : Parcelable {

  companion object {
    fun create() = HelpScreenModel(
        helpContent = null,
        helpPullResult = null
    )
  }

  val hasHelpContent: Boolean
    get() = helpContent != null

  val hasHelpPullResult: Boolean
    get() = helpPullResult != null

  fun helpContentLoaded(helpContent: String?): HelpScreenModel {
    return copy(helpContent = helpContent)
  }

  fun helpPullResultUpdated(helpPullResult: HelpPullResult): HelpScreenModel {
    return copy(helpPullResult = helpPullResult)
  }
}
