package org.simple.clinic.signature

import android.graphics.Bitmap
import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import java.io.File

class SignatureUpdateTest {

  private val bitmap = mock<Bitmap>()
  private val filePath = mock<File>()
  private val model = SignatureModel.create(filePath)
  private val updateSpec = UpdateSpec(SignatureUpdate())

  @Test
  fun `when clear signature button is clicked, then undo the signature drawn`() {
    updateSpec
        .given(model)
        .whenEvents(UndoClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ClearSignature as SignatureEffect)
            )
        )
  }

  @Test
  fun `when accept signature button is clicked, then save the file as PNG in local storage`() {
    updateSpec
        .given(model)
        .whenEvents(AcceptClicked(bitmap))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(AcceptSignature(bitmap, filePath) as SignatureEffect)
            )
        )
  }

  @Test
  fun `when the signature is successfully saved, then close the screen`() {
    updateSpec
        .given(model)
        .whenEvents(SignatureAccepted)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(CloseScreen as SignatureEffect)
            )
        )
  }

}
