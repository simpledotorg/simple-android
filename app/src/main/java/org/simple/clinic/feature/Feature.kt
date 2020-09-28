package org.simple.clinic.feature

enum class Feature(
    val enabledByDefault: Boolean,
    val remoteConfigKey: String = ""
) {
  EditBloodSugar(true),
  NotifyAppUpdateAvailable(false, "appupdate_enabled"),
  DeletePatient(true, "delete_patient_feature_enabled"),
  SecureCalling(false, "phonenumbermasker_masking_enabled"),
  LogSavedStateSizes(false, "log_saved_state_sizes_enabled")
}
