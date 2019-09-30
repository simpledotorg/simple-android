package org.simple.clinic.illustration

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Inject

class IllustrationConfigParser @Inject constructor(
    private val moshi: Moshi,
    private val configReader: ConfigReader
) {
  fun illustrations(): List<HomescreenIllustration> {
    val illustrationsType = Types.newParameterizedType(List::class.java, HomescreenIllustration::class.java)
    val mapType = Types.newParameterizedType(Map::class.java, String::class.java, illustrationsType)
    val adapter: JsonAdapter<Map<String, List<HomescreenIllustration>>> = moshi.adapter(mapType)

    val map = adapter.fromJson(configReader.string("home_screen_illustration", "{}"))
    return map?.get("IN") ?: emptyList()
  }
}
