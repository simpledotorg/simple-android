package org.simple.clinic.drugstockreminders

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DrugStockReportLastCheckedAt
import org.simple.clinic.main.TypedPreference.Type.IsDrugStockReportFilled
import org.simple.clinic.util.preference.BooleanPreferenceConverter
import org.simple.clinic.util.preference.InstantRxPreferencesConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.time.Instant
import java.util.Optional
import javax.inject.Named

@Module
object DrugStockReminderApiModule {

  @Provides
  fun providesApi(@Named("for_deployment") retrofit: Retrofit): DrugStockReminderApi {
    return retrofit.create(DrugStockReminderApi::class.java)
  }

  @TypedPreference(DrugStockReportLastCheckedAt)
  @Provides
  fun drugStockReportLastCheckAtPreference(rxSharedPreferences: RxSharedPreferences): Preference<Instant> {
    return rxSharedPreferences.getObject("drug_stock_report_last_checked_at", Instant.EPOCH, InstantRxPreferencesConverter())
  }

  @TypedPreference(IsDrugStockReportFilled)
  @Provides
  fun isDrugStockReportFilledPreference(rxSharedPreferences: RxSharedPreferences): Preference<Optional<Boolean>> {
    return rxSharedPreferences.getOptional("is_drug_stock_report_filled", BooleanPreferenceConverter(), Optional.empty())
  }
}
