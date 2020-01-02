package org.simple.clinic.illustration

import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class HomescreenIllustrationModule {

  @Provides
  @Named("homescreen-illustration-folder")
  fun illustrationsFolder() = "homescreen-illustrations"
}
