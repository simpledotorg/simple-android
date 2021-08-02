package org.simple.clinic.simplevideo

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import org.intellij.lang.annotations.Language
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.simplevideo.SimpleVideoConfig.Type.NumberOfPatientsRegistered
import org.simple.clinic.simplevideo.SimpleVideoConfig.Type.TrainingVideo
import java.util.Locale

@Module
class SimpleVideoModule {

  @Provides
  @SimpleVideoConfig(NumberOfPatientsRegistered)
  fun provideCountOfRegisteredPatients(rxSharedPreferences: RxSharedPreferences): Preference<Int> {
    return rxSharedPreferences.getInteger("number_of_patients_registered", 0)
  }

  @Provides
  @SimpleVideoConfig(TrainingVideo)
  fun provideSimpleVideoBasedOnLocale(
      remoteConfigReader: ConfigReader,
      locale: Locale,
      moshi: Moshi
  ): SimpleVideo {
    val type = Types.newParameterizedType(Map::class.java, String::class.java, SimpleVideo::class.java)
    val simpleVideoConfigAdapter = moshi.adapter<Map<String, SimpleVideo>>(type)

    @Language("JSON")
    val defaultSimpleVideoJson = """
      {
        "default": {
          "url": "https://www.youtube.com/watch?v=YO3D1paAuqU",
          "duration": "5:07"
        }
      }
    """
    val videoJson = remoteConfigReader.string("simple_youtube_video", defaultSimpleVideoJson)
    val simpleYouTubeVideos: Map<String, SimpleVideo> = simpleVideoConfigAdapter.fromJson(videoJson)!!

    return simpleYouTubeVideos[locale.language] ?: simpleYouTubeVideos["default"]!!
  }
}
