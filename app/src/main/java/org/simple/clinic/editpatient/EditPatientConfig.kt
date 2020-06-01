package org.simple.clinic.editpatient

import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Inject

data class EditPatientConfig(
    val deletePatientFeatureEnabled: Boolean
) {

  @Inject
  constructor(configReader: ConfigReader) : this(
      deletePatientFeatureEnabled = configReader.boolean("delete_patient_feature_enabled", true)
  )
}
