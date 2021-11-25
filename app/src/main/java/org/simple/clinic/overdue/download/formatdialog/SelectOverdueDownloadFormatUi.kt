package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.overdue.download.OverdueListFileFormat

interface SelectOverdueDownloadFormatUi {
  fun setOverdueListFormat(overdueListFileFormat: OverdueListFileFormat)
  fun setDownloadTitle()
  fun setDownloadButtonLabel()
  fun setShareTitle()
  fun setShareButtonLabel()
  fun hideTitle()
  fun hideContent()
  fun showProgress()
  fun hideDownloadOrShareButton()
  fun showTitle()
  fun showContent()
  fun hideProgress()
  fun showDownloadOrShareButton()
}
