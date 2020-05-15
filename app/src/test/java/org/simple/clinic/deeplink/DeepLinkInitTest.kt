package org.simple.clinic.deeplink

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class DeepLinkInitTest {

  @Test
  fun `when screen is created, then fetch user`() {
    val patientUuid = UUID.fromString("f3325419-d353-4e19-8ed6-fa138dfc0a03")
    val model = DeepLinkModel.default(patientUuid)
    val initSpec = InitSpec(DeepLinkInit())
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(FetchUser as DeepLinkEffect)
        ))
  }
}
