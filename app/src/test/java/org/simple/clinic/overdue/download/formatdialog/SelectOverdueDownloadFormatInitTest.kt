package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import java.util.UUID

class SelectOverdueDownloadFormatInitTest {

  @Test
  fun `When dialog is opened in progress for sharing, then update the model and start download for share`() {
    val initSpec = InitSpec(SelectOverdueDownloadFormatInit())
    val selectedAppointmentIds = setOf(UUID.fromString("669152f1-c854-4ca2-aa7e-dca4af3a44bf"))
    val progressForSharingModel = SelectOverdueDownloadFormatModel.create(SharingInProgress(selectedAppointmentIds))

    initSpec
        .whenInit(progressForSharingModel)
        .then(assertThatFirst(
            hasModel(progressForSharingModel.overdueDownloadInProgress()),
            hasEffects(DownloadForShare(CSV, selectedAppointmentIds))
        ))
  }
}
