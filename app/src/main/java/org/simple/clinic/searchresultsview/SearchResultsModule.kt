package org.simple.clinic.searchresultsview

import dagger.Module
import dagger.Provides

@Module
class SearchResultsModule {

  @Provides
  fun phoneNumberObfuscator(): PhoneNumberObfuscator {
    return IndianPhoneNumberObfuscator()
  }
}
