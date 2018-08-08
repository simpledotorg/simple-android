package org.simple.clinic

import dagger.Module
import dagger.Provides
import io.bloco.faker.Faker
import org.simple.clinic.di.AppScope

@Module
class FakerModule {

  @Provides
  @AppScope
  fun provideFaker() = Faker("en-IND")
}
