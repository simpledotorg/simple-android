package org.simple.clinic.summary

import androidx.viewbinding.ViewBinding
import com.xwray.groupie.viewbinding.BindableItem
import io.reactivex.subjects.Subject
import org.simple.clinic.widgets.UiEvent

abstract class GroupieItemWithUiEvents<B : ViewBinding>(adapterId: Long) : BindableItem<B>(adapterId) {

  abstract var uiEvents: Subject<UiEvent>
}
