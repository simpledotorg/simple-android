package org.simple.clinic.overdue.download

import android.net.Uri

sealed class OverdueListDownloadResult {

  data class DownloadSuccessful(val uri: Uri) : OverdueListDownloadResult()

  object NotEnoughStorage : OverdueListDownloadResult()

  object DownloadFailed : OverdueListDownloadResult()
}
