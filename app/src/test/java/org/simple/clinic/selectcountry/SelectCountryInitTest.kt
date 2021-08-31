package org.simple.clinic.selectcountry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData

class SelectCountryInitTest {

  private val spec = InitSpec(SelectCountryInit())

  private val defaultModel = SelectCountryModel.FETCHING

  @Test
  fun `when the screen is created, the app manifest should be fetched`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(FetchManifest as SelectCountryEffect)
        ))
  }

  @Test
  fun `when the screen is restored with the list of supported countries, do nothing`() {
    val countries = listOf(
        TestData.countryV2(
            isoCountryCode = "IN",
            deploymentEndPoint = "https://in.simple.org",
            displayName = "India",
            isdCode = "91"
        ),
        TestData.countryV2(
            isoCountryCode = "BD",
            deploymentEndPoint = "https://bd.simple.org",
            displayName = "Bangladesh",
            isdCode = "880"
        )
    )

    val model = defaultModel.manifestFetched(countries)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }
}
