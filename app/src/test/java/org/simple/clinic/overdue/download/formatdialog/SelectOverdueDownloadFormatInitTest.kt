package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class SelectOverdueDownloadFormatInitTest {

  @Test
  fun `when dialog is opened in progress for sharing, then update the model and load selected overdue appointment ids`() {
    val initSpec = InitSpec(SelectOverdueDownloadFormatInit())
    val progressForSharingModel = SelectOverdueDownloadFormatModel.create(SharingInProgress)

    initSpec
        .whenInit(progressForSharingModel)
        .then(assertThatFirst(
            hasModel(progressForSharingModel.overdueDownloadInProgress()),
            hasEffects(LoadSelectedOverdueAppointmentIds)
        ))
  }
}
