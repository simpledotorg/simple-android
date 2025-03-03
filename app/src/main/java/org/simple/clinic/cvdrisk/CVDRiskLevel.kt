package org.simple.clinic.cvdrisk

import androidx.compose.ui.graphics.Color
import org.simple.clinic.R

enum class CVDRiskLevel(val displayStringResId: Int, val color: Color) {
  LOW_HIGH(R.string.statin_alert_low_high_risk_patient_x, Color(0xFFFF7A00)),
  MEDIUM_HIGH(R.string.statin_alert_medium_high_risk_patient_x, Color(0xFFFF7A00)),
  HIGH(R.string.statin_alert_high_risk_patient_x, Color(0xFFFF3355)),
  VERY_HIGH(R.string.statin_alert_very_high_risk_range, Color(0xFFFF3355));

  companion object {
    fun compute(cvdRiskRange: CVDRiskRange): CVDRiskLevel {
      return when {
        cvdRiskRange.min < 5 -> LOW_HIGH
        cvdRiskRange.min < 10 -> MEDIUM_HIGH
        cvdRiskRange.min < 20 -> HIGH
        else -> VERY_HIGH
      }
    }
  }
}
