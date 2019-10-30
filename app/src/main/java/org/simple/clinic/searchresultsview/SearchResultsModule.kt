package org.simple.clinic.searchresultsview

import dagger.Module
import dagger.Provides

@Module
class SearchResultsModule {

  @Provides
  fun phoneNumberMasker(): PhoneNumberMasker {
    return IndianPhoneNumberMasker()
  }
}
