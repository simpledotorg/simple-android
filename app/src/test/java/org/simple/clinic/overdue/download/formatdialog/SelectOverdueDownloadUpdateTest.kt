package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.overdue.download.OverdueListDownloadFormat.CSV

class SelectOverdueDownloadUpdateTest {

  @Test
  fun `when download or share button is clicked and it's opened as share, then download for share`() {
    val overdueDownloadFormatUpdatedModel = SelectOverdueDownloadFormatModel
        .create(openAs = Share)
        .overdueListDownloadFormatUpdated(CSV)

    UpdateSpec(SelectOverdueDownloadUpdate())
        .given(overdueDownloadFormatUpdatedModel)
        .whenEvent(DownloadOrShareClicked)
        .then(assertThatNext(
            hasModel(overdueDownloadFormatUpdatedModel.overdueDownloadInProgress()),
            hasEffects(DownloadForShare(CSV))
        ))
  }
}
