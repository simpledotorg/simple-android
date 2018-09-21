package org.simple.clinic.home.overdue.appointmentreminder

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = AppointmentReminderSheet
typealias UiChange = (Ui) -> Unit

class AppointmentReminderSheetController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {
  override fun apply(upstream: Observable<UiEvent>): ObservableSource<UiChange> {
    TODO("not implemented")
  }
}
