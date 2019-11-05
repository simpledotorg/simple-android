package org.simple.clinic.selectcountry

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.ManifestFetchResult
import org.simple.clinic.appconfig.ManifestFetchSucceeded
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.net.URI

class SelectCountryEffectHandlerTest {

  private val repository = mock<AppConfigRepository>()

  private val effectHandler = SelectCountryEffectHandler.create(repository, TrampolineSchedulersProvider())
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `the app manifest must be fetched when the fetch manifest effect is received`() {
    // given
    whenever(repository.fetchAppManifest()) doReturn Single.never<ManifestFetchResult>()

    // when
    testCase.dispatch(FetchManifest)

    // then
    verify(repository).fetchAppManifest()
    verifyNoMoreInteractions(repository)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when fetching the app manifest succeeds, the manifest fetched event must be emitted`() {
    // given
    val countries = listOf(Country(
        code = "IN",
        endpoint = URI("https://in.simple.org"),
        displayName = "India",
        isdCode = "91"
    ))
    whenever(repository.fetchAppManifest()) doReturn Single.just<ManifestFetchResult>(ManifestFetchSucceeded(countries))

    // when
    testCase.dispatch(FetchManifest)

    // then
    testCase.assertOutgoingEvents(ManifestFetched(countries))
  }
}
