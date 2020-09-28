package org.simple.clinic.signature

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class SignatureInitTest {

  @Test
  fun `when screen is created, then load signature bitmap`() {
    val model = SignatureModel.create()

    InitSpec(SignatureInit())
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadSignatureBitmap)
        ))
  }
}
