package org.simple.clinic.di

import dagger.Binds
import dagger.Module
import org.simple.clinic.uuid.RealUuidGenerator
import org.simple.clinic.uuid.UuidGenerator

@Module
abstract class UuidGeneratorModule {

  @Binds
  abstract fun bindsUuidGenerator(uuidGenerator: RealUuidGenerator): UuidGenerator
}
