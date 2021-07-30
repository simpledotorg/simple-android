package org.simple.clinic.simplevideo

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.simplevideo.SimpleVideoConfig.Type.NumberOfPatientsRegistered
import org.simple.clinic.simplevideo.SimpleVideoConfig.Type.TrainingVideoYoutubeId
import java.util.Locale

@Module
class SimpleVideoModule {

  @Provides
  @SimpleVideoConfig(NumberOfPatientsRegistered)
  fun provideCountOfRegisteredPatients(rxSharedPreferences: RxSharedPreferences): Preference<Int> {
    return rxSharedPreferences.getInteger("number_of_patients_registered", 0)
  }

  @Provides
  @SimpleVideoConfig(TrainingVideoYoutubeId)
  fun provideSimpleVideoUrlBasedOnLocale(
      remoteConfigReader: ConfigReader,
      locale: Locale,
      moshi: Moshi
  ): String {
    val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    val stringMapAdapter = moshi.adapter<Map<String, String>>(type)

    val defaultVideoIdJson = """
    {
       "_default": "YO3D1paAuqU"
    }
    """
    val youtubeVideoIds: Map<String, String> = stringMapAdapter.fromJson(remoteConfigReader.string("simple_youtube_video_ids", defaultVideoIdJson))!!

    return (youtubeVideoIds[locale.language] ?: youtubeVideoIds["_default"]).toString()
  }
}
