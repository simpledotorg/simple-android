package org.simple.clinic.selectcountry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.appconfig.Country
import java.net.URI

class SelectCountryUiRendererTest {

  private val defaultModel = SelectCountryModel.FETCHING
  private val ui = mock<SelectCountryUi>()

  private val india = Country(
      isoCountryCode = "IN",
      endpoint = URI("https://in.simple.org"),
      displayName = "India",
      isdCode = "91"
  )
  private val bangladesh = Country(
      isoCountryCode = "BD",
      endpoint = URI("https://bd.simple.org"),
      displayName = "Bangladesh",
      isdCode = "880"
  )
  private val countries = listOf(india, bangladesh)

  private val renderer = SelectCountryUiRenderer(ui)

  @Test
  fun `when the model is being initialized, show the progress bar`() {
    // when
    renderer.render(defaultModel)

    // then
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest has been fetched, render the list of countries`() {
    // given
    val model = defaultModel.manifestFetched(countries)

    // when
    renderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).displaySupportedCountries(countries)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest fails to fetch with a network error, display the network error message and show the retry button`() {
    // given
    val model = defaultModel.manifestFetchError(NetworkError)

    // when
    renderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).displayNetworkErrorMessage()
    verify(ui).showRetryButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest fails to fetch with a server error, display the server error message and show the retry button`() {
    // given
    val model = defaultModel.manifestFetchError(ServerError)

    // when
    renderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).displayServerErrorMessage()
    verify(ui).showRetryButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest fails to fetch with any other error, display a generic error message and show the retry button`() {
    // given
    val model = defaultModel.manifestFetchError(UnexpectedError)

    // when
    renderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).displayGenericErrorMessage()
    verify(ui).showRetryButton()
    verifyNoMoreInteractions(ui)
  }
}
