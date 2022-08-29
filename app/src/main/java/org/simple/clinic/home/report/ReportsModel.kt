package org.simple.clinic.home.report

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.toNullable
import java.util.Optional

@Parcelize
data class ReportsModel(
    @IgnoredOnParcel
    val reportsContent: String? = null
) : Parcelable {

  companion object {
    fun create() = ReportsModel(reportsContent = null)
  }

  val hasLoadedReports: Boolean
    get() = reportsContent != null

  fun reportsContentLoaded(reportsContent: Optional<String>): ReportsModel {
    return copy(reportsContent = reportsContent.toNullable().orEmpty())
  }
}
