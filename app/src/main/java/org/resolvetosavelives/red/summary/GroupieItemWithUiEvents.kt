package org.resolvetosavelives.red.summary

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import org.resolvetosavelives.red.widgets.UiEvent

abstract class GroupieItemWithUiEvents<VH : ViewHolder>(adapterId: Long) : Item<VH>(adapterId) {

  abstract var uiEvents: Subject<UiEvent>
}
