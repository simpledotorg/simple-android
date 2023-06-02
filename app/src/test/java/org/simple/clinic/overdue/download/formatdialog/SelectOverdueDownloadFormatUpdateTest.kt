package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import org.mockito.kotlin.mock
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
import java.util.UUID

class SelectOverdueDownloadFormatUpdateTest {

  private val updateSpec = UpdateSpec(SelectOverdueDownloadFormatUpdate())
  private val selectedAppointmentIds = setOf(UUID.fromString("25594531-fc73-498c-9675-7e7eea4eda39"))
  private val defaultModel = SelectOverdueDownloadFormatModel.create(openAs = Share)

  @Test
  fun `when download or share button is clicked and it's opened as share, then load selected appointment ids`() {
    val overdueDownloadFormatUpdatedModel = defaultModel.overdueListDownloadFormatUpdated(CSV)

    updateSpec
        .given(overdueDownloadFormatUpdatedModel)
        .whenEvent(DownloadOrShareClicked)
        .then(assertThatNext(
            hasModel(overdueDownloadFormatUpdatedModel.overdueDownloadInProgress()),
            hasEffects(LoadSelectedOverdueAppointmentIds)
        ))
  }

  @Test
  fun `when download or share button is clicked and it's opened as download, then load selected appointment ids`() {
    val overdueDownloadFormatUpdatedModel = SelectOverdueDownloadFormatModel
        .create(Download)
        .overdueListDownloadFormatUpdated(CSV)

    updateSpec
        .given(overdueDownloadFormatUpdatedModel)
        .whenEvent(DownloadOrShareClicked)
        .then(assertThatNext(
            hasModel(overdueDownloadFormatUpdatedModel.overdueDownloadInProgress()),
            hasEffects(LoadSelectedOverdueAppointmentIds)
        ))
  }

  @Test
  fun `when download or share button is clicked and it's opened as share in progress, then do nothing`() {
    val overdueDownloadFormatUpdatedModel = SelectOverdueDownloadFormatModel
        .create(SharingInProgress)
        .overdueListDownloadFormatUpdated(CSV)

    updateSpec
        .given(overdueDownloadFormatUpdatedModel)
        .whenEvent(DownloadOrShareClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
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

  @Test
  fun `when selected appointments are loaded and dialog is opened for download, then schedule download`() {
    val defaultModel = SelectOverdueDownloadFormatModel
        .create(Download)
        .overdueListDownloadFormatUpdated(CSV)
        .overdueDownloadInProgress()

    updateSpec
        .given(defaultModel)
        .whenEvent(SelectedOverdueAppointmentsLoaded(selectedAppointmentIds))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ScheduleDownload(CSV))
        ))
  }

  @Test
  fun `when selected appointments are loaded and dialog is opened for share, then download for share`() {
    val defaultModel = SelectOverdueDownloadFormatModel
        .create(Share)
        .overdueListDownloadFormatUpdated(CSV)
        .overdueDownloadInProgress()

    updateSpec
        .given(defaultModel)
        .whenEvent(SelectedOverdueAppointmentsLoaded(selectedAppointmentIds))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(DownloadForShare(CSV, selectedAppointmentIds))
        ))
  }

  @Test
  fun `when selected appointments are loaded and dialog is opened for share in progress, then schedule download`() {
    val defaultModel = SelectOverdueDownloadFormatModel
        .create(SharingInProgress)
        .overdueListDownloadFormatUpdated(CSV)
        .overdueDownloadInProgress()

    updateSpec
        .given(defaultModel)
        .whenEvent(SelectedOverdueAppointmentsLoaded(selectedAppointmentIds))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(DownloadForShare(CSV, selectedAppointmentIds))
        ))
  }
}
