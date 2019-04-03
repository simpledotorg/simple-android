package org.simple.clinic.util.identifierdisplay

import dagger.Module
import dagger.Provides
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport

@Module
class IdentifierDisplayAdapterModule {

  @Provides
  fun provideIdentifierDisplayAdapter(
      bpPassportTextConverter: BpPassportTextConverter
  ): IdentifierDisplayAdapter {

    return IdentifierDisplayAdapter(
        converters = mapOf(
            BpPassport to bpPassportTextConverter
        )
    )
  }
}
