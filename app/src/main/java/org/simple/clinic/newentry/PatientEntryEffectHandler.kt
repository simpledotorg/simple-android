package org.simple.clinic.newentry

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.OngoingNewPatientEntry.Address
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.simplevideo.SimpleVideoConfig
import org.simple.clinic.simplevideo.SimpleVideoConfig.Type.NumberOfPatientsRegistered
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientEntryEffectHandler @AssistedInject constructor(
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    private val inputFieldsFactory: InputFieldsFactory,
    @SimpleVideoConfig(NumberOfPatientsRegistered) private val patientRegisteredCount: Preference<Int>,
    @Assisted private val viewEffectsConsumer: Consumer<PatientEntryViewEffect>
) {

  @AssistedFactory
  interface InjectionFactory {
    fun create(
        viewEffectsConsumer: Consumer<PatientEntryViewEffect>
    ): PatientEntryEffectHandler
  }

  fun build(): ObservableTransformer<PatientEntryEffect, PatientEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientEntryEffect, PatientEntryEvent>()
        .addTransformer(FetchPatientEntry::class.java, fetchOngoingEntryTransformer(schedulersProvider.io()))
        .addTransformer(SavePatient::class.java, savePatientTransformer(schedulersProvider.io()))
        .addTransformer(LoadInputFields::class.java, loadInputFields())
        .addTransformer(FetchColonyOrVillagesEffect::class.java, fetchColonyOrVillages())
        .addConsumer(PatientEntryViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun fetchColonyOrVillages(): ObservableTransformer<FetchColonyOrVillagesEffect, PatientEntryEvent> {
    return ObservableTransformer { fetchColonyOrVillagesEffect ->
      fetchColonyOrVillagesEffect
          .map { patientRepository.allColoniesOrVillagesInPatientAddress() }
          .map(::ColonyOrVillagesFetched)
    }
  }

  private fun fetchOngoingEntryTransformer(scheduler: Scheduler): ObservableTransformer<FetchPatientEntry, PatientEntryEvent> {
    return ObservableTransformer { fetchPatientEntries ->
      val getPatientEntryAndFacility = Singles
          .zip(
              Single.just(patientRepository.ongoingEntry()),
              facilityRepository.currentFacility().firstOrError()
          )

      fetchPatientEntries
          .flatMapSingle { getPatientEntryAndFacility }
          .subscribeOn(scheduler)
          .map { (entry, facility) ->
            if (entry.address != null && entry.address.doesNotHaveDistrictAndState) {
              entry.withDistrict(facility.district).withState(facility.state)
            } else if (entry.address != null) {
              entry
            } else {
              entry.withAddress(Address.withDistrictAndState(facility.district, facility.state))
            }
          }
          .map { OngoingEntryFetched(it) }
    }
  }

  private fun savePatientTransformer(scheduler: Scheduler): ObservableTransformer<SavePatient, PatientEntryEvent> {
    return ObservableTransformer { savePatientEffects ->
      savePatientEffects
          .map { it.entry }
          .subscribeOn(scheduler)
          .doOnNext(patientRepository::saveOngoingEntry)
          .doOnNext { patientRegisteredCount.set(patientRegisteredCount.get().plus(1)) }
          .map { PatientEntrySaved }
    }
  }

  private fun loadInputFields(): ObservableTransformer<LoadInputFields, PatientEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { inputFieldsFactory.provideFields() }
          .map(::InputFields)
          .map(::InputFieldsLoaded)
    }
  }
}
