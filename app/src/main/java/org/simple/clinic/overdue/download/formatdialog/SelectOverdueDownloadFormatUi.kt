package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.overdue.download.OverdueListDownloadFormat

interface SelectOverdueDownloadFormatUi {
  fun setOverdueListFormat(overdueListDownloadFormat: OverdueListDownloadFormat)
  fun setDownloadTitle()
  fun setDownloadButtonLabel()
  fun setShareTitle()
  fun setShareButtonLabel()
}
