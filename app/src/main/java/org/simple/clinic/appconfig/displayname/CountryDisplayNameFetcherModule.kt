package org.simple.clinic.appconfig.displayname

import dagger.Binds
import dagger.Module

@Module
abstract class CountryDisplayNameFetcherModule {

  @Binds
  abstract fun bindsDisplayNameFetcher(displayNameFetcher: PlatformCountryDisplayNameFetcher): CountryDisplayNameFetcher
}
