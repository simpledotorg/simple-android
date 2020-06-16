package org.simple.clinic.storage.text

import dagger.Binds
import dagger.Module

@Module
abstract class TextStoreModule {

  @Binds
  abstract fun bindTextStore(store: LocalDbTextStore): TextStore
}
