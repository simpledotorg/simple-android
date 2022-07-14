package org.simple.clinic.overdue

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.simple.clinic.di.AppScope
import java.util.UUID
import javax.inject.Inject

@AppScope
class OverdueAppointmentSelector @Inject constructor() {

  private val _selectedAppointmentIdsStream = BehaviorSubject.createDefault<HashSet<UUID>>(hashSetOf())
  val selectedAppointmentIdsStream: Observable<Set<UUID>>
    get() = this._selectedAppointmentIdsStream.map { it.toSet() }

  private val selectedAppointmentIds = hashSetOf<UUID>()

  fun toggleSelection(appointmentId: UUID) {
    if (selectedAppointmentIds.contains(appointmentId)) {
      selectedAppointmentIds.remove(appointmentId)
    } else {
      selectedAppointmentIds.add(appointmentId)
    }
    _selectedAppointmentIdsStream.onNext(selectedAppointmentIds)
  }

  fun replaceSelectedIds(newSelectedAppointmentIds: Set<UUID>) {
    selectedAppointmentIds.clear()
    selectedAppointmentIds.addAll(newSelectedAppointmentIds)
    _selectedAppointmentIdsStream.onNext(selectedAppointmentIds)
  }

  fun clearSelection() {
    selectedAppointmentIds.clear()
    _selectedAppointmentIdsStream.onNext(hashSetOf())
  }

  fun addSelectedIds(newSelectedAppointmentIds: Set<UUID>) {
    selectedAppointmentIds.addAll(newSelectedAppointmentIds)
    _selectedAppointmentIdsStream.onNext(selectedAppointmentIds)
  }
}
