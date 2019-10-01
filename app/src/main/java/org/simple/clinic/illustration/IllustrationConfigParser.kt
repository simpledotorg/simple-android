package org.simple.clinic.illustration

import androidx.annotation.VisibleForTesting
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Inject

class IllustrationConfigParser @Inject constructor(
    moshi: Moshi,
    private val configReader: ConfigReader
) {

  private val illustrationMapJsonAdapter: JsonAdapter<Map<String, List<HomescreenIllustration>>>

  init {
    val illustrationsType = Types.newParameterizedType(List::class.java, HomescreenIllustration::class.java)
    val mapType = Types.newParameterizedType(Map::class.java, String::class.java, illustrationsType)

    illustrationMapJsonAdapter = moshi.adapter(mapType)
  }

  companion object {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val EMPTY_JSON = "{}"
  }

  fun illustrations(): List<HomescreenIllustration> {
    val illustrationConfigJson = configReader.string("home_screen_illustration", EMPTY_JSON)

    val map: Map<String, List<HomescreenIllustration>> = illustrationMapJsonAdapter.fromJson(illustrationConfigJson)!!
    return map.getOrElse("IN") { emptyList() }
  }
}
