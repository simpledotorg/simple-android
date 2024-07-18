package org.simple.clinic.overdue.download

import android.net.Uri

sealed class OverdueListDownloadResult {

  data class DownloadSuccessful(val uri: Uri) : OverdueListDownloadResult()

  data object NotEnoughStorage : OverdueListDownloadResult()

  data object DownloadFailed : OverdueListDownloadResult()
}
