package org.simple.clinic.help

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
class HelpModule {

  @Provides
  fun helpApi(retrofit: Retrofit): HelpApi = retrofit.create(HelpApi::class.java)

  @Provides
  @Named("help_file_path")
  fun helpFilePath() = "help.html"
}
