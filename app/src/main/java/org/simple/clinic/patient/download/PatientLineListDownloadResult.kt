package org.simple.clinic.patient.download

import android.net.Uri

sealed class PatientLineListDownloadResult {

  data class DownloadSuccessful(val uri: Uri) : PatientLineListDownloadResult()

  object NotEnoughStorage : PatientLineListDownloadResult()

  object DownloadFailed : PatientLineListDownloadResult()
}
