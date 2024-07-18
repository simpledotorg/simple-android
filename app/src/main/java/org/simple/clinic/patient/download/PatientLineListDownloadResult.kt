package org.simple.clinic.patient.download

import android.net.Uri

sealed class PatientLineListDownloadResult {

  data class DownloadSuccessful(val uri: Uri) : PatientLineListDownloadResult()

  data object NotEnoughStorage : PatientLineListDownloadResult()

  data object DownloadFailed : PatientLineListDownloadResult()
}
