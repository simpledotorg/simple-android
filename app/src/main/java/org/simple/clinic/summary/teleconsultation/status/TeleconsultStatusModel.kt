package org.simple.clinic.summary.teleconsultation.status

import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus

data class TeleconsultStatusModel(
    val teleconsultStatus: TeleconsultStatus?
) {

  companion object {

    fun create() = TeleconsultStatusModel(
        teleconsultStatus = null
    )
  }

  val hasTeleconsultStatus: Boolean
    get() = teleconsultStatus != null

  fun teleconsultStatusChanged(teleconsultStatus: TeleconsultStatus): TeleconsultStatusModel {
    return copy(teleconsultStatus = teleconsultStatus)
  }
}
