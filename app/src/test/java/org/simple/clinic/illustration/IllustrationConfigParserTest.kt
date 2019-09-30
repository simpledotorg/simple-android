package org.simple.clinic.illustration

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.squareup.moshi.Moshi
import org.junit.Test
import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Month

class IllustrationConfigParserTest {

  private val configReader: ConfigReader = mock()

  private val moshi = Moshi.Builder()
      .add(DayOfMonth.MoshiTypeAdapter)
      .build()

  private val illustrationConfigParser = IllustrationConfigParser(
      moshi = moshi,
      configReader = configReader
  )

  @Test
  fun `verify illustration config parsing happens as expected`() {
    val illustrationsConfig = """
{
  "IN": [
    {
      "eventId": "gandhi_jayanthi",
      "illustrationUrl": "https://unsplash.com/something.jpg",
      "from": "06 June",
      "to": "14 June"
    }
  ]
}
    """
    whenever(configReader.string("home_screen_illustration", "{}")).thenReturn(illustrationsConfig)
    val illustrations = illustrationConfigParser.illustrations()

    assertThat(illustrations).isEqualTo(listOf(
        HomescreenIllustration(
            eventId = "gandhi_jayanthi",
            illustrationUrl = "https://unsplash.com/something.jpg",
            from = DayOfMonth(6, Month.JUNE),
            to = DayOfMonth(14, Month.JUNE)
        )
    ))
  }

  @Test
  fun `verify illustration config parsing happens when empty object is passed`() {
    whenever(configReader.string("home_screen_illustration", "{}")).thenReturn("{}")
    val illustrations = illustrationConfigParser.illustrations()

    assertThat(illustrations).isEqualTo(emptyList<HomescreenIllustration>())
  }
}
