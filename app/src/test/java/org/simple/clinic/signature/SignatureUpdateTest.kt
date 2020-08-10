package org.simple.clinic.signature

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class SignatureUpdateTest {

  @Test
  fun `when clear signature button is clicked, then undo the signature drawn`() {
    val model = SignatureModel.create()
    UpdateSpec(SignatureUpdate())
        .given(model)
        .whenEvents(UndoClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ClearSignature as SignatureEffect)
            )
        )
  }
}
