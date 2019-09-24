package org.simple.clinic.illustration

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.remoteconfig.ConfigReader

@Module
class HomescreenIllustrationModule {

  @Provides
  fun illustrationDao(appDatabase: AppDatabase) = appDatabase.illustrationDao()

  @Provides
  fun illustrations(
      configReader: ConfigReader,
      moshi: Moshi
  ): List<HomescreenIllustration> {
    val parameterizedType = Types.newParameterizedType(List::class.java, HomescreenIllustration::class.java)
    val adapter = moshi.adapter<List<HomescreenIllustration>>(parameterizedType)
    val illustrations = adapter.fromJson(configReader.string("home_screen_illustration", "[]"))
    return checkNotNull(illustrations)
  }
}
