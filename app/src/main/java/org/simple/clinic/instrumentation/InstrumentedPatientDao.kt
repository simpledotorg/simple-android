package org.simple.clinic.instrumentation

import com.google.firebase.perf.FirebasePerformance
import com.newrelic.agent.android.NewRelic
import com.newrelic.agent.android.instrumentation.MetricCategory
import com.newrelic.agent.android.metric.MetricUnit
import io.reactivex.Flowable
import io.reactivex.Scheduler
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.minus
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class InstrumentedPatientDao @Inject constructor(
    private val tracingConfig: TracingConfig,
    private val dao: Patient.RoomDao,
    private val schedulersProvider: SchedulersProvider
) : Patient.RoomDao() {

  private fun recordMetric(name: String, duration: Duration) {
    if (tracingConfig.enabled) {
      val secondsAsDouble = duration.toMillis() / 1000.0

      NewRelic.recordMetric(
          name,
          MetricCategory.DATABASE.categoryName,
          1,
          secondsAsDouble,
          secondsAsDouble,
          MetricUnit.OPERATIONS,
          MetricUnit.SECONDS
      )
    }
  }

  override fun allPatients(): Flowable<List<Patient>> {
    return dao.allPatients().measure("Load all patients")
  }

  override fun getOne(uuid: UUID): Patient? {
    return measureTimedValue("Get one patient") { dao.getOne(uuid) }
  }

  override fun patient(uuid: UUID): Flowable<List<Patient>> {
    return dao.patient(uuid).measure("Load patient")
  }

  override fun updateSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus) {
    measureTimedValue("Update all patients sync status") { dao.updateSyncStatus(oldStatus, newStatus) }
  }

  override fun updateSyncStatus(uuids: List<UUID>, newStatus: SyncStatus) {
    measureTimedValue("Update sync status for specific patients") { dao.updateSyncStatus(uuids, newStatus) }
  }

  override fun patientCount(): Flowable<Int> {
    return dao.patientCount().measure("Get count of all patients")
  }

  override fun patientCount(syncStatus: SyncStatus): Flowable<Int> {
    return dao.patientCount(syncStatus).measure("Get count of patients with sync status")
  }

  override fun clear() {
    measureTimedValue("Clear patients") { dao.clear() }
  }

  override fun findPatientsWithBusinessId(identifier: String): Flowable<List<Patient>> {
    return dao.findPatientsWithBusinessId(identifier).measure("Find patients with business ID")
  }

  override fun updatePatientStatus(uuid: UUID, newStatus: PatientStatus, newSyncStatus: SyncStatus, newUpdatedAt: Instant) {
    measureTimedValue("Update sync status for a specific patient") { dao.updatePatientStatus(uuid, newStatus, newSyncStatus, newUpdatedAt) }
  }

  override fun compareAndUpdateRecordedAt(patientUuid: UUID, instantToCompare: Instant, updatedAt: Instant, pendingStatus: SyncStatus) {
    measureTimedValue("Compare and update recorded time for a specific patient") { dao.compareAndUpdateRecordedAt(patientUuid, instantToCompare, updatedAt, pendingStatus) }
  }

  override fun updateRecordedAt(patientUuid: UUID, updatedAt: Instant, pendingStatus: SyncStatus) {
    measureTimedValue("Set recorded time to earliest measurement for a specific patient") { dao.updateRecordedAt(patientUuid, updatedAt, pendingStatus) }
  }

  override fun loadPatientQueryModelsWithSyncStatus(syncStatus: SyncStatus): Flowable<List<PatientQueryModel>> {
    return dao.loadPatientQueryModelsWithSyncStatus(syncStatus).measure("Load patient query models with sync status")
  }

  override fun loadPatientQueryModelsForPatientUuid(patientUuid: UUID): Flowable<List<PatientQueryModel>> {
    return dao.loadPatientQueryModelsForPatientUuid(patientUuid).measure("Load patient query model for specific patient")
  }

  override fun isPatientDefaulter(patientUuid: UUID, yesAnswer: Answer, scheduled: Appointment.Status): Flowable<Boolean> {
    return dao.isPatientDefaulter(patientUuid, yesAnswer, scheduled).measure("Load if patient is a defaulter")
  }

  override fun hasPatientChangedSince(patientUuid: UUID, instantToCompare: Instant, pendingStatus: SyncStatus): Boolean {
    return measureTimedValue("Check if patient was updated since") { dao.hasPatientChangedSince(patientUuid, instantToCompare, pendingStatus) }
  }

  override fun insert(record: List<Patient>): List<Long> {
    return measureTimedValue("Bulk insert patients") { dao.insert(record) }
  }

  override fun update(entities: List<Patient>) {
    return measureTimedValue("Bulk update patients") { dao.update(entities) }
  }

  private fun <T> Flowable<T>.measure(
      operation: String,
      scheduler: Scheduler = schedulersProvider.computation()
  ): Flowable<T> {
    return this
        .compose { upstream ->
          Flowable.just(upstream)
              .timestamp(scheduler)
              .flatMap { start ->
                var alreadyReportedToAnalytics = false
                val trace = FirebasePerformance.getInstance().newTrace(operation)

                start
                    .value()
                    .timestamp(scheduler)
                    .doOnSubscribe { trace.start() }
                    .doOnNext { end ->
                      if (!alreadyReportedToAnalytics) {
                        val timeTaken = (end - start)
                        recordMetric(operation, timeTaken)
                        trace.incrementMetric("durationMs", timeTaken.toMillis())

                        trace.stop()
                        alreadyReportedToAnalytics = true
                      }
                    }
                    .map { it.value() }
              }
        }
  }

  private fun <T> measureTimedValue(
      metricName: String,
      block: () -> T
  ): T {
    val trace = FirebasePerformance.getInstance().newTrace(metricName)
    trace.start()
    val start = Instant.now()

    val value = block()

    val end = Instant.now()

    val duration = Duration.between(start, end)
    trace.incrementMetric("durationMs", duration.toMillis())
    recordMetric(metricName, duration)

    trace.stop()

    return value
  }
}
