package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri

interface UiActions {
  fun shareDownloadedFile(downloadedUri: Uri)
  fun dismiss()
}
