package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap
import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class TeleconsultDoctorInfoUpdateTest {

  private val model = TeleconsultDoctorInfoModel.create()
  private val updateSpec = UpdateSpec(TeleconsultDoctorInfoUpdate())

  @Test
  fun `when medical registration id is loaded, then set medical registration id and update model`() {
    val medicalRegistrationId = "1234567890"

    updateSpec
        .given(model)
        .whenEvent(MedicalRegistrationIdLoaded(medicalRegistrationId))
        .then(assertThatNext(
            hasModel(model.medicalRegistrationIdLoaded(medicalRegistrationId)),
            hasEffects(SetMedicalRegistrationId(medicalRegistrationId))
        ))
  }

  @Test
  fun `when signature bitmap is loaded, then set signature bitmap`() {
    val signatureBitmap = mock<Bitmap>()

    updateSpec
        .given(model)
        .whenEvent(SignatureBitmapLoaded(signatureBitmap))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetSignatureBitmap(signatureBitmap))
        ))
  }
}
