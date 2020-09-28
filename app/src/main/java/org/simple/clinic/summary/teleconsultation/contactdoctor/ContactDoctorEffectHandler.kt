package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRequestInfo
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import javax.inject.Inject

class ContactDoctorEffectHandler @Inject constructor(
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val teleconsultationFacilityRepository: TeleconsultationFacilityRepository,
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val uuidGenerator: UuidGenerator,
    private val clock: UtcClock,
    private val schedulersProvider: SchedulersProvider
) {

  fun build() = RxMobius
      .subtypeEffectHandler<ContactDoctorEffect, ContactDoctorEvent>()
      .addTransformer(LoadMedicalOfficers::class.java, loadMedicalOfficers())
      .addTransformer(CreateTeleconsultRequest::class.java, createTeleconsultRequest())
      .build()

  private fun createTeleconsultRequest(): ObservableTransformer<CreateTeleconsultRequest, ContactDoctorEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val teleconsultRecordId = uuidGenerator.v4()
            val teleconsultRequestInfo = TeleconsultRequestInfo(
                requesterId = currentUser.get().uuid,
                facilityId = currentFacility.get().uuid,
                requestedAt = Instant.now(clock)
            )

            teleconsultRecordRepository.createTeleconsultRequestForNurse(
                teleconsultRecordId = teleconsultRecordId,
                patientUuid = it.patientUuid,
                medicalOfficerId = it.medicalOfficerId,
                teleconsultRequestInfo = teleconsultRequestInfo
            )

            teleconsultRecordId to it
          }
          .map { (teleconsultRecordId, effect) ->
            TeleconsultRequestCreated(
                teleconsultRecordId = teleconsultRecordId,
                doctorPhoneNumber = effect.doctorPhoneNumber
            )
          }
    }
  }

  private fun loadMedicalOfficers(): ObservableTransformer<LoadMedicalOfficers, ContactDoctorEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { teleconsultationFacilityRepository.medicalOfficersForFacility(currentFacility.get().uuid) }
          .map(::MedicalOfficersLoaded)
    }
  }
}
