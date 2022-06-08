package org.simple.clinic.overdue

import org.simple.clinic.remoteconfig.ConfigReader

data class PendingAppointmentsConfig(
    val pendingListDefaultStateSize: Int
) {

  companion object {

    fun read(configReader: ConfigReader): PendingAppointmentsConfig {
      val numberOfPendingPatients = configReader
          .long("pending_list_default_state_size", 10L)
          .coerceAtLeast(1L)
          .toInt()

      return PendingAppointmentsConfig(
          pendingListDefaultStateSize = numberOfPendingPatients
      )
    }
  }
}
