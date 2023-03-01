package org.simple.clinic.monthlyscreeningreports.complete

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MonthlyScreeningReportCompleteModel(
    val month: String?
) : Parcelable {
  companion object {
    fun default() = MonthlyScreeningReportCompleteModel(
        month = null
    )
  }
}
