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
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.RemoteConfigService
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Named

@Module
class InputFieldsFactoryModule {

  @Provides
  fun provideInputFieldsProvider(
      country: Country,
      @Named("date_for_user_input") dateTimeFormatter: DateTimeFormatter,
      userClock: UserClock,
      currentFacility: Lazy<Facility>,
      chennaiFacilityGroupIds: Lazy<Set<UUID>>
  ): InputFieldsProvider {
    val date = LocalDate.now(userClock)

    return when (val isoCountryCode = country.isoCountryCode) {
      Country.INDIA -> IndiaInputFieldsProvider(dateTimeFormatter, date, currentFacility, chennaiFacilityGroupIds)
      Country.BANGLADESH -> BangladeshInputFieldsProvider(dateTimeFormatter, date)
      Country.ETHIOPIA -> EthiopiaInputFieldsProvider(dateTimeFormatter, date)
      else -> throw IllegalArgumentException("Unknown country code: $isoCountryCode")
    }
  }

  @Provides
  fun readChennaiFacilityGroupIds(
      uuidSetJsonAdapter: JsonAdapter<Set<UUID>>,
      remoteConfigService: RemoteConfigService,
      crashReporter: CrashReporter
  ): Set<UUID> {
    val chennaiFacilityIdJsonArray = remoteConfigService.reader().string("chennai_facility_group_ids", "[]")

    return try {
      uuidSetJsonAdapter.fromJson(chennaiFacilityIdJsonArray)!!
    } catch (e: Exception) {
      // We do not want crash the app in this scenario, just report
      // the exception and go with the default behaviour.
      crashReporter.report(e)
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
