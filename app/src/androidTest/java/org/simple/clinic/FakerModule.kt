package org.simple.clinic

import dagger.Module
import dagger.Provides
import io.bloco.faker.Faker
import io.bloco.faker.components.PhoneNumber
import org.simple.clinic.di.AppScope

@Module
class FakerModule {

  @Provides
  @AppScope
  fun provideFaker(): Faker = Faker("en-IND")

  @Provides
  fun provideFakePhoneNumber(faker: Faker): PhoneNumber = faker.phoneNumber
}
