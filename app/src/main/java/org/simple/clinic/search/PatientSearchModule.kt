package org.simple.clinic.search

import dagger.Module
import dagger.Provides
import org.simple.clinic.search.results.IndianPhoneNumberObfuscator
import org.simple.clinic.search.results.PhoneNumberObfuscator

@Module
class PatientSearchModule {

  @Provides
  fun phoneNumberObfuscator(): PhoneNumberObfuscator {
    return IndianPhoneNumberObfuscator()
  }
}
