package org.simple.clinic.cvdrisk

object CVDRiskUtil {
  fun getMinRisk(risk: String): Int {
    val range = risk.split("-").map { it.trim().toInt() }
    return range.min()
  }

  fun getMaxRisk(risk: String): Int {
    val range = risk.split("-").map { it.trim().toInt() }
    return range.max()
  }
}
