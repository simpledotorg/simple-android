package org.simple.clinic.signature

import android.graphics.Bitmap
import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.NextMatchers.hasNothing
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class SignatureUpdateTest {

  private val bitmap = mock<Bitmap>()
  private val model = SignatureModel.create()
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
                hasEffects(AcceptSignature(bitmap) as SignatureEffect)
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

  @Test
  fun `when signature is loaded, then set signature`() {
    val bitmap = mock<Bitmap>()

    updateSpec
        .given(model)
        .whenEvents(SignatureBitmapLoaded(bitmap))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SetSignatureBitmap(bitmap) as SignatureEffect)
            )
        )
  }

  @Test
  fun `when no signature is loaded, then do nothing`() {
    updateSpec
        .given(model)
        .whenEvents(SignatureBitmapLoaded(null))
        .then(
            assertThatNext(hasNothing())
        )
  }
}
