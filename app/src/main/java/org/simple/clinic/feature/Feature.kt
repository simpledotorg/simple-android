package org.simple.clinic.feature

import android.os.Build

enum class Feature(
    val enabledByDefault: Boolean,
    val remoteConfigKey: String = ""
) {
  EditBloodSugar(true),
  NotifyAppUpdateAvailable(false, "appupdate_enabled"),
  SecureCalling(false, "phonenumbermasker_masking_enabled"),

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
  HttpRequestBodyCompression(false, "http_request_body_compression_enabled"),
  CallResultSyncEnabled(true),
  NotifyAppUpdateAvailableV2(false, "appupdate_enabled_v2"),
  OverdueInstantSearch(false, "overdue_instant_search_v2"),
  PatientReassignment(false, "patient_reassignment_v0"),
  PatientStatinNudge(false, "patient_statin_nudge_v0"),
  NonLabBasedStatinNudge(false, "non_lab_based_statin_nudge")
}
