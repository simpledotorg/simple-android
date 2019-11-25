package org.simple.clinic.util.identifierdisplay

import dagger.Module
import dagger.Provides
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown

@Module
class IdentifierDisplayAdapterModule {

  @Provides
  fun provideIdentifierDisplayAdapter(
      bpPassportDisplayFormatter: BpPassportDisplayFormatter,
      bangladeshNationalIdDisplayFormatter: BangladeshNationalIdDisplayFormatter,
      unknownDisplayFormatter: UnknownDisplayFormatter
  ) = IdentifierDisplayAdapter(
      formatters = mapOf(
          BpPassport::class.java to bpPassportDisplayFormatter,
          BangladeshNationalId::class.java to bangladeshNationalIdDisplayFormatter,
          Unknown::class.java to unknownDisplayFormatter
      )
  )
}
