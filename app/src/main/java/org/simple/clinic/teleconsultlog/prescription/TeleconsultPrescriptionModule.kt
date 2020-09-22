package org.simple.clinic.teleconsultlog.prescription

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.MedicalRegistrationId
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.teleconsultlog.prescription.medicines.TeleconsultMedicinesConfig
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.OptionalRxPreferencesConverter
import org.simple.clinic.util.preference.StringPreferenceConverter
import java.time.Duration

@Module
object TeleconsultPrescriptionModule {

  @Provides
  @TypedPreference(MedicalRegistrationId)
  fun providesMedicalRegistrationId(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("medical_registration_id", Optional.empty(), OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }

  @Provides
  fun providesTeleconsultMedicinesConfig(): TeleconsultMedicinesConfig {
    return TeleconsultMedicinesConfig(
        defaultDuration = Duration.ofDays(30),
        defaultFrequency = MedicineFrequency.OD
    )
  }
}
