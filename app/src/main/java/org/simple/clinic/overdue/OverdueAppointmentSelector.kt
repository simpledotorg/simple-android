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

  private val _selectedAppointmentIds = hashSetOf<UUID>()
  val selectedAppointmentIds: Set<UUID>
    get() = _selectedAppointmentIds

  fun toggleSelection(appointmentId: UUID) {
    if (_selectedAppointmentIds.contains(appointmentId)) {
      _selectedAppointmentIds.remove(appointmentId)
    } else {
      _selectedAppointmentIds.add(appointmentId)
    }
    _selectedAppointmentIdsStream.onNext(_selectedAppointmentIds)
  }

  fun replaceSelectedIds(newSelectedAppointmentIds: Set<UUID>) {
    _selectedAppointmentIds.clear()
    _selectedAppointmentIds.addAll(newSelectedAppointmentIds)
    _selectedAppointmentIdsStream.onNext(_selectedAppointmentIds)
  }

  fun clearSelection() {
    _selectedAppointmentIds.clear()
    _selectedAppointmentIdsStream.onNext(hashSetOf())
  }

  fun addSelectedIds(newSelectedAppointmentIds: Set<UUID>) {
    _selectedAppointmentIds.addAll(newSelectedAppointmentIds)
    _selectedAppointmentIdsStream.onNext(_selectedAppointmentIds)
  }
}
