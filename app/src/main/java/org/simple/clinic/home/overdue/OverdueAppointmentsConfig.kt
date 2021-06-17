package org.simple.clinic.home.overdue

import org.simple.clinic.remoteconfig.ConfigReader

data class OverdueAppointmentsConfig(
    val overdueAppointmentsLoadSize: Int
) {

  companion object {
    fun read(configReader: ConfigReader): OverdueAppointmentsConfig {
      val overdueAppointmentsLoadSize = configReader.long("overdue_appointments_load_size", 15)

      return OverdueAppointmentsConfig(
          overdueAppointmentsLoadSize = overdueAppointmentsLoadSize.toInt()
      )
    }
  }
}
