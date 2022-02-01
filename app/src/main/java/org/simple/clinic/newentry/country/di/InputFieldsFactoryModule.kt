package org.simple.clinic.newentry.country.di

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Lazy
import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.Facility
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.EthiopiaInputFieldsProvider
import org.simple.clinic.newentry.country.IndiaInputFieldsProvider
import org.simple.clinic.newentry.country.InputFieldsProvider
import org.simple.clinic.newentry.country.SriLankaInputFieldsProvider
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.RemoteConfigService
import java.util.UUID

@Module
class InputFieldsFactoryModule {

  @Provides
  fun provideInputFieldsProvider(
      country: Country,
      currentFacility: Lazy<Facility>,
      chennaiFacilityGroupIds: Lazy<Set<UUID>>
  ): InputFieldsProvider {
    return when (val isoCountryCode = country.isoCountryCode) {
      Country.DEMO,
      Country.INDIA -> IndiaInputFieldsProvider(currentFacility, chennaiFacilityGroupIds)
      Country.BANGLADESH -> BangladeshInputFieldsProvider()
      Country.ETHIOPIA -> EthiopiaInputFieldsProvider()
      Country.SRI_LANKA -> SriLankaInputFieldsProvider()
      else -> throw IllegalArgumentException("Unknown country code: $isoCountryCode")
    }
  }

  @Provides
  fun readChennaiFacilityGroupIds(
      uuidSetJsonAdapter: JsonAdapter<Set<UUID>>,
      remoteConfigService: RemoteConfigService,
  ): Set<UUID> {
    val chennaiFacilityIdJsonArray = remoteConfigService.reader().string("chennai_facility_group_ids", "[]")

    return try {
      uuidSetJsonAdapter.fromJson(chennaiFacilityIdJsonArray)!!
    } catch (e: Exception) {
      // We do not want crash the app in this scenario, just report
      // the exception and go with the default behaviour.
      CrashReporter.report(e)
      emptySet()
    }
  }

  @Provides
  fun provideUuidJsonAdapter(
      moshi: Moshi
  ): JsonAdapter<Set<UUID>> {
    val type = Types.newParameterizedType(Set::class.java, UUID::class.java)
    return moshi.adapter(type)
  }
}
