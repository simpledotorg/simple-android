package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class ContactDoctorEffectHandler @Inject constructor(
    private val currentFacility: Lazy<Facility>,
    private val teleconsultationFacilityRepository: TeleconsultationFacilityRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build() = RxMobius
      .subtypeEffectHandler<ContactDoctorEffect, ContactDoctorEvent>()
      .addTransformer(LoadMedicalOfficers::class.java, loadMedicalOfficers())
      .build()

  private fun loadMedicalOfficers(): ObservableTransformer<LoadMedicalOfficers, ContactDoctorEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { teleconsultationFacilityRepository.medicalOfficersForFacility(currentFacility.get().uuid) }
          .map(::MedicalOfficersLoaded)
    }
  }
}
