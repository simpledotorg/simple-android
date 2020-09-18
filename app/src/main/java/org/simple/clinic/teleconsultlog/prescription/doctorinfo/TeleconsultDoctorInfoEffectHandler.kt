package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.MedicalRegistrationId
import org.simple.clinic.util.Optional
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultDoctorInfoEffectHandler @AssistedInject constructor(
    @TypedPreference(MedicalRegistrationId) private val medicalRegistrationIdPreference: Preference<Optional<String>>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TeleconsultDoctorInfoUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: TeleconsultDoctorInfoUiActions): TeleconsultDoctorInfoEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultDoctorInfoEffect, TeleconsultDoctorInfoEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultDoctorInfoEffect, TeleconsultDoctorInfoEvent>()
        .addTransformer(LoadMedicalRegistrationId::class.java, loadMedicalRegistrationId())
        .addConsumer(SetMedicalRegistrationId::class.java, { uiActions.setMedicalRegistrationId(it.medicalRegistrationId) }, schedulersProvider.ui())
        .build()
  }

  private fun loadMedicalRegistrationId(): ObservableTransformer<LoadMedicalRegistrationId, TeleconsultDoctorInfoEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { medicalRegistrationIdPreference.get() }
          .extractIfPresent()
          .map(::MedicalRegistrationIdLoaded)
    }
  }
}
