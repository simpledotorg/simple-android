package org.simple.clinic.drugstockreminders

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.UpdateDrugStockReportsMonth
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
object DrugStockReminderApiModule {

  @Provides
  fun providesApi(@Named("for_deployment") retrofit: Retrofit): DrugStockReminderApi {
    return retrofit.create(DrugStockReminderApi::class.java)
  }

  @TypedPreference(UpdateDrugStockReportsMonth)
  @Provides
  fun updateDrugStockReportsMonth(
      rxSharedPreferences: RxSharedPreferences
  ): Preference<Optional<String>> {
    return rxSharedPreferences.getOptional("drug_stock_report_month", StringPreferenceConverter())
  }
}
