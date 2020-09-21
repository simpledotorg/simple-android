package org.simple.clinic.teleconsultlog.prescription

import dagger.Module
import dagger.Provides
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.teleconsultlog.prescription.medicines.TeleconsultMedicinesConfig
import java.time.Duration

@Module
object TeleconsultPrescriptionModule {

  @Provides
  fun providesTeleconsultMedicinesConfig(): TeleconsultMedicinesConfig {
    return TeleconsultMedicinesConfig(
        defaultDuration = Duration.ofDays(30),
        defaultFrequency = MedicineFrequency.OD
    )
  }
}
