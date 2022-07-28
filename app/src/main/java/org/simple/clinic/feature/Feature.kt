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
  OverdueCount(true, "overdue_count"),
  VillageTypeAhead(false, "village_type_ahead"),
  InstantSearchQrCode(true, "instant_search_qr_code"),
  EthiopianCalendar(true, "ethiopian_calendar"),
  IndiaNationalHealthID(true, "india_national_health_id"),
  OverdueListDownloadAndShare(true, "download_and_share_overdue_list"),
  CustomDrugSearchScreen(true, "drug_search_screen"),
  OnlinePatientLookup(true, "online_patient_lookup"),
  HttpRequestBodyCompression(false, "http_request_body_compression_enabled"),
  CallResultSyncEnabled(true),
  NextAppointment(false, "next_appointment_v1"),
  AddingHealthIDsFromEditPatient(false, "adding_health_ids_from_edit_patient"),
  NotifyAppUpdateAvailableV2(false, "appupdate_enabled_v2"),
  MonthlyDrugStockReportReminder(false, "monthly_drug_stock_report_reminders_v1"),
  OverdueSections(false, "overdue_section_improvements_v1"),
  OverdueInstantSearch(false, "overdue_instant_search_v1"),
  OverdueSearchV2(false, "overdue_search_v2"),
  OverdueSelectAndDownload(false, "overdue_select_and_download_v1")
}
