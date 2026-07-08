package org.simple.clinic.summary.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.cvdrisk.CVDRiskRange

@Parcelize
data class CVDRiskInfo(
    val canShowCVDRisk: Boolean,
    val cvdRisk: CVDRiskRange?,
    val hasCVD: Boolean,
    val hasDiabetes: Boolean,
) : Parcelable {
  companion object {
    fun default(): CVDRiskInfo {
      return CVDRiskInfo(
          canShowCVDRisk = false,
          cvdRisk = null,
          hasCVD = false,
          hasDiabetes = false
      )
    }
  }
}

