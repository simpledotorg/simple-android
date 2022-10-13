package org.simple.clinic.patient.download.formatdialog

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.download.PatientLineListFileFormat


class SelectLineListFormatUpdateTest {

  private val updateSpec = UpdateSpec(SelectLineListFormatUpdate())
  private val defaultModel = SelectLineListFormatModel.create()

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

  @Test
  fun `when download file format is changed, then update the model`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadFileFormatChanged(PatientLineListFileFormat.CSV))
        .then(assertThatNext(
            hasModel(defaultModel.fileFormatChanged(PatientLineListFileFormat.CSV)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when cancel button is clicked, then dismiss the dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CancelButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(Dismiss)
        ))
  }
}
