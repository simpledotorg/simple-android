package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.communication.CommunicationRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.sync.indicator.SyncIndicatorState.ConnectToSync
import org.simple.clinic.sync.indicator.SyncIndicatorState.SyncPending
import org.simple.clinic.sync.indicator.SyncIndicatorState.Synced
import org.simple.clinic.sync.indicator.SyncIndicatorState.Syncing
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

typealias Ui = SyncIndicatorView
typealias UiChange = (Ui) -> Unit

class SyncIndicatorViewController @Inject constructor(
    private val lastSyncState: Preference<LastSyncedState>,
    private val utcClock: UtcClock,
    private val configProvider: Observable<SyncIndicatorConfig>,
    private val dataSync: DataSync,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val appointmentRepository: AppointmentRepository,
    private val communicationRepository: CommunicationRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        updateIndicatorView(replayedEvents),
        startSync(replayedEvents),
        showPendingSyncStatus(replayedEvents))
  }

  private fun updateIndicatorView(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreated = events.ofType<SyncIndicatorViewCreated>()
    val lastSyncedStateStream = lastSyncState
        .asObservable()
        .distinctUntilChanged()

    val showDefaultSyncIndicatorState = lastSyncedStateStream
        .filter { it.lastSyncProgress == null }
        .map { { ui: Ui -> ui.updateState(SyncPending) } }

    val syncProgress = lastSyncedStateStream.filter { it.lastSyncProgress != null }

    val showSyncIndicatorState = Observables
        .combineLatest(screenCreated, syncProgress, configProvider)
        { _, stateStream, config ->
          when (stateStream.lastSyncProgress!!) {
            SUCCESS, FAILURE -> syncIndicatorState(stateStream, config.syncFailureThreshold)
            SYNCING -> Syncing
          }
        }
        .switchMap {
          if (it is Synced) {
            val durationSince = it.durationSince
            Observable.interval(1, TimeUnit.MINUTES)
                .map { min -> { ui: Ui -> ui.updateState(Synced(durationSince.plusMinutes(min))) } }
                .startWith { ui: Ui -> ui.updateState(Synced(durationSince)) }
          } else {
            Observable.just { ui: Ui -> ui.updateState(it) }
          }
        }

    return showSyncIndicatorState.mergeWith(showDefaultSyncIndicatorState)
  }

  private fun syncIndicatorState(syncState: LastSyncedState, maxIntervalSinceLastSync: Duration): SyncIndicatorState {
    val timestamp = syncState.lastSyncSucceededAt ?: return SyncPending

    val now = Instant.now(utcClock)
    val timeSinceLastSync = Duration.between(timestamp, now)
    val mostFrequentSyncInterval = enumValues<SyncInterval>()
        .map { it.frequency }
        .min()!!

    val syncHappenedInTheFuture = timeSinceLastSync.isNegative

    return when {
      timeSinceLastSync > maxIntervalSinceLastSync -> ConnectToSync
      timeSinceLastSync > mostFrequentSyncInterval -> SyncPending
      syncHappenedInTheFuture -> SyncPending
      else -> when (syncState.lastSyncProgress!!) {
        SUCCESS -> Synced(timeSinceLastSync)
        FAILURE -> SyncPending
        SYNCING -> Syncing
      }
    }
  }


  private fun startSync(events: Observable<UiEvent>): Observable<UiChange> {
    val errorsStream = {
      dataSync
          .streamSyncErrors()
          .take(1)
          .map { { ui: Ui -> ui.showErrorDialog(it) } }
    }

    val lastSyncedStateStream = lastSyncState
        .asObservable()
        .distinctUntilChanged()

    val syncStream = {
      dataSync
          .sync(SyncGroup.FREQUENT)
          .toObservable<UiChange>()
    }

    return events
        .ofType<SyncIndicatorViewClicked>()
        .withLatestFrom(lastSyncedStateStream)
        .filter { (_, lastSyncedState) ->
          lastSyncedState.lastSyncProgress == null || lastSyncedState.lastSyncProgress != SYNCING
        }
        .switchMap { syncStream().mergeWith(errorsStream()) }
  }

  private fun showPendingSyncStatus(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events.ofType<SyncIndicatorViewCreated>()

    val isPatientSyncPending = screenCreates
        .flatMap {
          patientRepository
              .pendingRecordsCount()
              .map { count -> count > 0 }
        }

    val isBpSyncPending = screenCreates
        .flatMap {
          bloodPressureRepository
              .pendingRecordsCount()
              .map { count -> count > 0 }
        }

    val isPrescriptionSyncPending = screenCreates
        .flatMap {
          prescriptionRepository
              .pendingRecordsCount()
              .map { count -> count > 0 }
        }

    val isMedicalHistorySyncPending = screenCreates
        .flatMap {
          medicalHistoryRepository
              .pendingRecordsCount()
              .map { count -> count > 0 }
        }

    val isAppointmentSyncPending = screenCreates
        .flatMap {
          appointmentRepository
              .pendingRecordsCount()
              .map { count -> count > 0 }
        }

    val isCommunicationSyncPending = screenCreates
        .flatMap {
          communicationRepository
              .pendingRecordsCount()
              .map { count -> count > 0 }
        }

    return Observable
        .mergeArray(
            isPatientSyncPending,
            isBpSyncPending,
            isPrescriptionSyncPending,
            isAppointmentSyncPending,
            isMedicalHistorySyncPending,
            isCommunicationSyncPending
        )
        .filter { isSyncPending -> isSyncPending }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.updateState(SyncPending) } }
  }
}
