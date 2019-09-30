package org.simple.clinic.illustration

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
class HomescreenIllustrationModule {

  @Provides
  @Named("homescreen-illustration-folder")
  fun illustrationsFolder() = "homescreen-illustrations"

  @Provides
  fun fileDownloadService(retrofit: Retrofit): FileDownloadService = retrofit.create(FileDownloadService::class.java)
}
