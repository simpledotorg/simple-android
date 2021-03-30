package org.simple.clinic.feature

import android.os.Build

enum class Feature(
    val enabledByDefault: Boolean,
    val remoteConfigKey: String = ""
) {
  EditBloodSugar(true),
  NotifyAppUpdateAvailable(false, "appupdate_enabled"),
  DeletePatient(true, "delete_patient_feature_enabled"),
  SecureCalling(false, "phonenumbermasker_masking_enabled"),
  LogSavedStateSizes(false, "log_saved_state_sizes_enabled"),

  /**
   * API levels 21 and 22 cause a framework level crash in appcompat 1.2.0 when overriding
   * the configuration locale if a WebView is present in the layout. Since critical features
   * in the app depend on a WebView, we will disable the change language feature for these
   * API levels until this issue is resolved.
   *
   * There's a corresponding activity [org.simple.clinic.WebviewTestActivity], which you
   * can run to verify the fix.
   **/
  ChangeLanguage(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M),
  MLKitQrCodeScanner(true, "ml_kit_qr_code_scanner"),
  InstantSearch(true, "instant_search"),
  OverdueCount(true, "overdue_count"),
  VillageTypeAhead(false, "village_type_ahead"),
  InstantSearchQrCode(true, "instant_search_qr_code"),
  EthiopianCalendar(true, "ethiopian_calendar")
}
