package org.simple.clinic.patient

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.simple.clinic.patient.businessid.BusinessIdMetaData
import org.simple.clinic.patient.businessid.BusinessIdMetaDataAdapter
import org.simple.clinic.patient.businessid.MoshiBusinessIdMetaDataAdapter
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.remoteconfig.ConfigReader

@Module
class PatientModule {

  @Provides
  fun providePatientConfig(configReader: ConfigReader) = PatientConfig.read(configReader)

  @Provides
  fun phoneNumberMaskerConfig(reader: ConfigReader): PhoneNumberMaskerConfig = PhoneNumberMaskerConfig.read(reader)

  @Provides
  fun provideBusinessIdMetaAdapter(moshi: Moshi): BusinessIdMetaDataAdapter {
    return MoshiBusinessIdMetaDataAdapter(moshi.adapter(BusinessIdMetaData::class.java))
  }
}
