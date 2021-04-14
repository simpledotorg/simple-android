package org.simple.clinic.home.report

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toNullable

@Parcelize
data class ReportsModel(val reportsContent: String?) : Parcelable {

  companion object {
    fun create() = ReportsModel(reportsContent = null)
  }

  val hasLoadedReports: Boolean
    get() = reportsContent != null

  fun reportsContentLoaded(reportsContent: Optional<String>): ReportsModel {
    return copy(reportsContent = reportsContent.toNullable().orEmpty())
  }
}
