package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap
import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

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

  @Test
  fun `when medical registration id changes, then update the model`() {
    val oldMedicalRegistrationId = "1234567890"
    val newMedicalRegistrationId = "0987654321"

    val oldMedicalRegistrationIdModel = model.medicalRegistrationIdLoaded(oldMedicalRegistrationId)

    updateSpec
        .given(oldMedicalRegistrationIdModel)
        .whenEvent(MedicalRegistrationIdChanged(newMedicalRegistrationId))
        .then(assertThatNext(
            hasModel(oldMedicalRegistrationIdModel.medicalRegistrationIdLoaded(newMedicalRegistrationId)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when instructions changes, then update the model`() {
    val instructions = "This is a medical instructions given by doctor"

    updateSpec
        .given(model)
        .whenEvent(MedicalInstructionsChanged(instructions))
        .then(assertThatNext(
            hasModel(model.medicalInstructionsChanged(instructions)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when current user is loaded, then update the model`() {
    val currentUser = TestData.loggedInUser(
        uuid = UUID.fromString("6dedf026-c0ca-4977-8785-6a8833a19964")
    )

    updateSpec
        .given(model)
        .whenEvent(CurrentUserLoaded(currentUser))
        .then(assertThatNext(
            hasModel(model.currentUserLoaded(currentUser)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when add signature button is clicked, then show the add signature dialog`() {
    updateSpec
        .given(model)
        .whenEvent(AddSignatureClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAddSignatureDialog)
        ))
  }

  @Test
  fun `when activity is resumed, then load the signature`() {
    updateSpec
        .given(model)
        .whenEvent(ActivityResumed)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadSignatureBitmap)
        ))
  }
}
