package org.simple.clinic.bloodsugar.entry

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.cast
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.entry.PrefillDate.PrefillSpecificDate
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class BloodSugarEntryEffectHandler @AssistedInject constructor(
    private val bloodSugarRepository: BloodSugarRepository,
    private val patientRepository: PatientRepository,
    private val appointmentsRepository: AppointmentRepository,
    private val userClock: UserClock,
    private val schedulersProvider: SchedulersProvider,
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>,
    @Assisted private val viewEffectsConsumer: Consumer<BloodSugarEntryViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<BloodSugarEntryViewEffect>): BloodSugarEntryEffectHandler
  }

  private val reportAnalyticsEvents = ReportAnalyticsEvents()

  fun build(): ObservableTransformer<BloodSugarEntryEffect, BloodSugarEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarEntryEffect, BloodSugarEntryEvent>()
        .addTransformer(PrefillDate::class.java, prefillDate(schedulersProvider.ui()))
        .addTransformer(CreateNewBloodSugarEntry::class.java, createNewBloodSugarEntryTransformer())
        .addTransformer(FetchBloodSugarMeasurement::class.java, fetchBloodSugarMeasurement(schedulersProvider.io()))
        .addTransformer(UpdateBloodSugarEntry::class.java, updateBloodSugarEntryTransformer(schedulersProvider.io()))
        .addTransformer(LoadBloodSugarUnitPreference::class.java, loadBloodSugarUnitPreference())
        .addConsumer(BloodSugarEntryViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadBloodSugarUnitPreference(): ObservableTransformer<LoadBloodSugarUnitPreference, BloodSugarEntryEvent> {
    return ObservableTransformer { effect ->
      effect
          .observeOn(schedulersProvider.io())
          .switchMap { bloodSugarUnitPreference.asObservable() }
          .map(::BloodSugarUnitPreferenceLoaded)
    }
  }

  private fun fetchBloodSugarMeasurement(
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodSugarMeasurement, BloodSugarEntryEvent> {
    return ObservableTransformer { fetchBloodSugarMeasurementEffectStream ->
      fetchBloodSugarMeasurementEffectStream
          .observeOn(scheduler)
          .map { getExistingBloodSugarMeasurement(it.bloodSugarMeasurementUuid) }
          .map { BloodSugarMeasurementFetched(it) }
    }
  }

  private fun getExistingBloodSugarMeasurement(bloodSugarMeasurementUuid: UUID): BloodSugarMeasurement? =
      bloodSugarRepository.measurement(bloodSugarMeasurementUuid)

  private fun prefillDate(scheduler: Scheduler): ObservableTransformer<PrefillDate, BloodSugarEntryEvent> {
    return ObservableTransformer { prefillDates ->
      prefillDates
          .map(::convertToLocalDate)
          .observeOn(scheduler)
          .doOnNext { viewEffectsConsumer.accept(PrefillDates(it)) }
          .map { DatePrefilled(it) }
    }
  }

  private fun convertToLocalDate(prefillDate: PrefillDate): LocalDate {
    val instant = if (prefillDate is PrefillSpecificDate) prefillDate.date else Instant.now(userClock)
    return instant.toLocalDateAtZone(userClock.zone)
  }

  private fun createNewBloodSugarEntryTransformer(): ObservableTransformer<CreateNewBloodSugarEntry, BloodSugarEntryEvent> {
    return ObservableTransformer { createNewBloodSugarEntries ->
      createNewBloodSugarEntries
          .observeOn(schedulersProvider.io())
          .flatMapSingle { createNewBloodSugarEntry ->
            val user = currentUser.get()
            val facility = currentFacility.get()
            storeNewBloodSugarMeasurement(user, facility, createNewBloodSugarEntry)
                .flatMap { updateAppointmentsAsVisited(createNewBloodSugarEntry, it) }
          }
          .compose(reportAnalyticsEvents)
          .cast()
    }
  }

  private fun storeNewBloodSugarMeasurement(
      user: User,
      currentFacility: Facility,
      entry: CreateNewBloodSugarEntry
  ): Single<BloodSugarMeasurement> {
    val (patientUuid, date, _, reading) = entry
    return bloodSugarRepository.saveMeasurement(
        uuid = uuidGenerator.v4(),
        reading = reading,
        patientUuid = patientUuid,
        loggedInUser = user,
        facility = currentFacility,
        recordedAt = date.toUtcInstant(userClock)
    )
  }

  private fun updateAppointmentsAsVisited(
      createNewBloodSugarEntry: CreateNewBloodSugarEntry,
      bloodSugarMeasurement: BloodSugarMeasurement
  ): Single<BloodSugarSaved> {
    val entryDate = createNewBloodSugarEntry.userEnteredDate.toUtcInstant(userClock)

    appointmentsRepository.markAppointmentsCreatedBeforeTodayAsVisited(bloodSugarMeasurement.patientUuid)
    patientRepository.compareAndUpdateRecordedAt(bloodSugarMeasurement.patientUuid, entryDate)

    return Single.just(BloodSugarSaved(createNewBloodSugarEntry.wasDateChanged))
  }

  private fun updateBloodSugarEntryTransformer(scheduler: Scheduler): ObservableTransformer<UpdateBloodSugarEntry, BloodSugarEntryEvent> {
    return ObservableTransformer { updateBloodSugarEntries ->
      updateBloodSugarEntries
          .observeOn(scheduler)
          .map { updateBloodSugarEntry ->
            val updatedBloodSugarMeasurement = updateBloodSugarMeasurement(updateBloodSugarEntry)
            storeUpdateBloodSugarMeasurement(updatedBloodSugarMeasurement)
            BloodSugarSaved(updateBloodSugarEntry.wasDateChanged)
          }
          .compose(reportAnalyticsEvents)
          .cast<BloodSugarEntryEvent>()
    }
  }

  private fun updateBloodSugarMeasurement(updateBloodSugarEntry: UpdateBloodSugarEntry): BloodSugarMeasurement {
    val (_, userEnteredDate, _, bloodSugarReading) = updateBloodSugarEntry
    val bloodSugarMeasurement = getExistingBloodSugarMeasurement(updateBloodSugarEntry.bloodSugarMeasurementUuid)!!
    val user = currentUser.get()
    val facility = currentFacility.get()

    return bloodSugarMeasurement.copy(
        userUuid = user.uuid,
        facilityUuid = facility!!.uuid,
        reading = bloodSugarMeasurement.reading.copy(value = bloodSugarReading.value),
        recordedAt = userEnteredDate.toUtcInstant(userClock)
    )
  }

  private fun storeUpdateBloodSugarMeasurement(bloodSugarMeasurement: BloodSugarMeasurement) {
    bloodSugarRepository.updateMeasurement(bloodSugarMeasurement)
    patientRepository.compareAndUpdateRecordedAt(bloodSugarMeasurement.patientUuid, bloodSugarMeasurement.recordedAt)
  }
}
