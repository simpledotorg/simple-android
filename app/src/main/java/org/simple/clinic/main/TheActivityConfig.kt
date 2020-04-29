package org.simple.clinic.main

import org.simple.clinic.remoteconfig.ConfigReader

data class TheActivityConfig(val shouldLogSavedStateSizes: Boolean) {

  companion object {
    fun read(configReader: ConfigReader): TheActivityConfig {
      val shouldLogSavedStateSizes = configReader.boolean("log_saved_state_sizes_enabled", default = false)

      return TheActivityConfig(shouldLogSavedStateSizes)
    }
  }
}
