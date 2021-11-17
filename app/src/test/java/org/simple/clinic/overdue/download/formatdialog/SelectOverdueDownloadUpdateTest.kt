package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.overdue.download.OverdueListDownloadFormat.CSV

class SelectOverdueDownloadUpdateTest {

  private val updateSpec = UpdateSpec(SelectOverdueDownloadUpdate())
  private val defaultModel = SelectOverdueDownloadFormatModel.create(openAs = Share)

  @Test
  fun `when download or share button is clicked and it's opened as share, then download for share`() {
    val overdueDownloadFormatUpdatedModel = defaultModel.overdueListDownloadFormatUpdated(CSV)

    updateSpec
        .given(overdueDownloadFormatUpdatedModel)
        .whenEvent(DownloadOrShareClicked)
        .then(assertThatNext(
            hasModel(overdueDownloadFormatUpdatedModel.overdueDownloadInProgress()),
            hasEffects(DownloadForShare(CSV))
        ))
  }

  @Test
  fun `when file is downloaded for sharing, then share the downloaded file`() {
    val uri = mock<Uri>()

    updateSpec
        .given(defaultModel)
        .whenEvent(FileDownloadedForSharing(uri))
        .then(assertThatNext(
            hasModel(defaultModel.overdueDownloadCompleted()),
            hasEffects(ShareDownloadedFile(uri))
        ))
  }
}
