package org.simple.clinic.patient

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import java.util.Locale
import javax.inject.Named

@Module
class SimpleVideoModule {

  @Provides
  @Named("number_of_patients_registered")
  fun provideCountOfRegisteredPatients(rxSharedPreferences: RxSharedPreferences): Preference<Int> {
    return rxSharedPreferences.getInteger("number_of_patients_registered", 0)
  }

  @Provides
  @Named("training_video_youtube_id")
  fun provideSimpleVideoUrlBasedOnLocale(locale: Locale): String {
    return when (locale.language) {
      "hi" -> "nHsQ06tiLzw"
      // Default to English
      else -> "YO3D1paAuqU"
    }
  }
}
