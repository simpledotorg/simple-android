package org.simple.clinic.summary

import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import io.reactivex.subjects.Subject
import org.simple.clinic.widgets.UiEvent

abstract class GroupieItemWithUiEvents<VH : GroupieViewHolder>(adapterId: Long) : Item<VH>(adapterId) {

  abstract var uiEvents: Subject<UiEvent>
}
