package org.simple.clinic.patient

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.simple.clinic.patient.businessid.BusinessId
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
    @Suppress("UNCHECKED_CAST")
    val adapters: Map<BusinessId.MetaDataVersion, JsonAdapter<BusinessIdMetaData>> = mapOf(
        BusinessId.MetaDataVersion.BpPassportMetaDataV1 to
            moshi.adapter(BusinessIdMetaData.BpPassportMetaDataV1::class.java) as JsonAdapter<BusinessIdMetaData>,
        BusinessId.MetaDataVersion.BangladeshNationalIdMetaDataV1 to
            moshi.adapter(BusinessIdMetaData.BangladeshNationalIdMetaDataV1::class.java) as JsonAdapter<BusinessIdMetaData>
    )

    return MoshiBusinessIdMetaDataAdapter(adapters)
  }
}
