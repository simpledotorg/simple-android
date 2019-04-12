package org.simple.clinic.util.identifierdisplay

import dagger.Module
import dagger.Provides
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown

@Module
class IdentifierDisplayAdapterModule {

  @Provides
  fun provideIdentifierDisplayAdapter(
      bpPassportDisplayFormatter: BpPassportDisplayFormatter,
      unknownDisplayFormatter: UnknownDisplayFormatter
  ): IdentifierDisplayAdapter {

    return IdentifierDisplayAdapter(
        formatters = mapOf(
            BpPassport::class.java to bpPassportDisplayFormatter,
            Unknown::class.java to unknownDisplayFormatter
        )
    )
  }
}
