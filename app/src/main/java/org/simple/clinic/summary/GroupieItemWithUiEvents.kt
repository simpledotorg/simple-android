package org.simple.clinic.summary

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import org.simple.clinic.widgets.UiEvent

abstract class GroupieItemWithUiEvents<VH : ViewHolder>(adapterId: Long) : Item<VH>(adapterId) {

  abstract var uiEvents: Subject<UiEvent>
}
