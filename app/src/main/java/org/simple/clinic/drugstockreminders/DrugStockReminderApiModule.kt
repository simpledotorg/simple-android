package org.simple.clinic.drugstockreminders

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
object DrugStockReminderApiModule {

  @Provides
  fun providesApi(@Named("for_deployment") retrofit: Retrofit): DrugStockReminderApi {
    return retrofit.create(DrugStockReminderApi::class.java)
  }
}
