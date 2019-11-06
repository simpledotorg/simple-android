package org.simple.clinic.appconfig

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Test
import org.simple.clinic.appconfig.api.ManifestFetchApi
import java.net.URI

class AppConfigRepositoryTest {

  @Test
  fun `successful network calls to fetch the app manifest should return the app manifest`() {
    // given
    val countries = listOf(
        Country(code = "IN", endpoint = URI("https://in.simple.org"), displayName = "India", isdCode = "91"),
        Country(code = "BD", endpoint = URI("https://bd.simple.org"), displayName = "Bangladesh", isdCode = "880")
    )

    val api = mock<ManifestFetchApi>()
    whenever(api.fetchManifest()).thenReturn(Single.just(countries))

    val repository = AppConfigRepository(api)

    // then

    repository
        .fetchAppManifest()
        .test()
        .assertValue(FetchSucceeded(countries))
        .assertNoErrors()
  }
}
