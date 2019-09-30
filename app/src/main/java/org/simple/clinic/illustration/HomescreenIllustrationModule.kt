package org.simple.clinic.illustration

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import retrofit2.Retrofit
import javax.inject.Named

@Module
class HomescreenIllustrationModule {

  @Provides
  @Named("homescreen-illustration-folder")
  fun illustrationsFolder() = "homescreen-illustrations"

  @Provides
  fun fileDownloadService(retrofit: Retrofit): FileDownloadService = retrofit.create(FileDownloadService::class.java)

  @Provides
  fun illustrations(
      moshi: Moshi,
      configReader: ConfigReader
  ): List<HomescreenIllustration> {
    val illustrationsType = Types.newParameterizedType(List::class.java, HomescreenIllustration::class.java)
    val mapType = Types.newParameterizedType(Map::class.java, String::class.java, illustrationsType)
    val adapter: JsonAdapter<Map<String, List<HomescreenIllustration>>> = moshi.adapter(mapType)

    val map = adapter.fromJson(configReader.string("home_screen_illustration", "{}"))
    return map?.get("IN") ?: emptyList()
  }
}
