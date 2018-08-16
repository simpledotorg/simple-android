package org.simple.clinic.sms

import dagger.Binds
import dagger.Module

@Module
abstract class SmsReaderModule {

  @Binds
  abstract fun bindSmsReader(smsReader: SmsReaderImpl): SmsReader
}
