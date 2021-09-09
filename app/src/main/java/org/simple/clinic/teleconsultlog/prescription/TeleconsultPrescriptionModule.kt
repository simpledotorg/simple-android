package org.simple.clinic.teleconsultlog.prescription

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.MedicalRegistrationId
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.teleconsultlog.prescription.medicines.TeleconsultMedicinesConfig
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import java.time.Duration
import java.util.Optional

@Module
object TeleconsultPrescriptionModule {

  @Provides
  @TypedPreference(MedicalRegistrationId)
  fun providesMedicalRegistrationId(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("medical_registration_id", StringPreferenceConverter())
  }

  @Provides
  fun providesTeleconsultMedicinesConfig(): TeleconsultMedicinesConfig {
    return TeleconsultMedicinesConfig(
        defaultDuration = Duration.ofDays(30),
        defaultFrequency = MedicineFrequency.OD
    )
  }
}
