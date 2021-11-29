package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.overdue.download.OverdueListDownloadResult.DownloadFailed
import org.simple.clinic.overdue.download.OverdueListDownloadResult.DownloadSuccessful
import org.simple.clinic.overdue.download.OverdueListDownloadResult.NotEnoughStorage
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import org.simple.clinic.overdue.download.OverdueListFileFormat.PDF

class SelectOverdueDownloadFormatUpdateTest {

  private val updateSpec = UpdateSpec(SelectOverdueDownloadFormatUpdate())
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
  fun `when download or share button is clicked and it's opened as download, then schedule the download`() {
    val overdueDownloadFormatUpdatedModel = SelectOverdueDownloadFormatModel
        .create(Download)
        .overdueListDownloadFormatUpdated(CSV)

    updateSpec
        .given(overdueDownloadFormatUpdatedModel)
        .whenEvent(DownloadOrShareClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ScheduleDownload(CSV))
        ))
  }

  @Test
  fun `when file is downloaded for sharing, then share the downloaded file`() {
    val uri = mock<Uri>()

    updateSpec
        .given(defaultModel)
        .whenEvent(FileDownloadedForSharing(DownloadSuccessful(uri)))
        .then(assertThatNext(
            hasModel(defaultModel.overdueDownloadCompleted()),
            hasEffects(ShareDownloadedFile(uri, defaultModel.overdueListFileFormat.mimeType))
        ))
  }

  @Test
  fun `when download is scheduled, then dismiss the dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueDownloadScheduled)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(Dismiss)
        ))
  }

  @Test
  fun `when cancel is clicked, then dismiss the dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CancelClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(Dismiss)
        ))
  }

  @Test
  fun `when download format is changed, then update the model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadFormatChanged(PDF))
        .then(assertThatNext(
            hasModel(defaultModel.overdueListDownloadFormatUpdated(PDF)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when sheet is opened as progress for sharing and download or share button is clicked, then do nothing`() {
    val progressForSharingModel = SelectOverdueDownloadFormatModel.create(SharingInProgress)

    updateSpec
        .given(progressForSharingModel)
        .whenEvent(DownloadOrShareClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }

   @Test
  fun `when there is not enough space to download the file, then open not enough storage error dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(FileDownloadedForSharing(NotEnoughStorage))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenNotEnoughStorageErrorDialog)
        ))
  }

  @Test
  fun `when download fails, then open download failed error dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(FileDownloadedForSharing(DownloadFailed))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenDownloadFailedErrorDialog)
        ))
  }
}
