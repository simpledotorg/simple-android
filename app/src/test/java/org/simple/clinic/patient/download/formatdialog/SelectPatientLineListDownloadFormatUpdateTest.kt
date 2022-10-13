package org.simple.clinic.patient.download.formatdialog

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.download.PatientLineListFileFormat

class SelectPatientLineListDownloadFormatUpdateTest {

  private val updateSpec = UpdateSpec(SelectPatientLineListDownloadFormatUpdate())
  private val defaultModel = SelectPatientLineListDownloadFormatModel.create()

  @Test
  fun `when download button is clicked, then start download`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                SchedulePatientLineListDownload(fileFormat = PatientLineListFileFormat.PDF)
            )
        ))
  }
}
