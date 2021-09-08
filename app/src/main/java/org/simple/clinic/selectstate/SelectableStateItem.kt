package org.simple.clinic.selectstate

import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.appconfig.State
import org.simple.clinic.databinding.ListSelectstateStateViewBinding
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

data class SelectableStateItem(
    private val state: State,
    private val isStateSelectedByUser: Boolean,
    private val showDivider: Boolean
) : ItemAdapter.Item<SelectableStateItem.StateClicked> {

  companion object {

    fun from(
        states: List<State>,
        selectedState: State?
    ): List<SelectableStateItem> {
      return states
          .mapIndexed { index, state ->
            val isLastStateInList = index == states.lastIndex
            val hasUserSelectedCountry = state == selectedState

            SelectableStateItem(
                state = state,
                isStateSelectedByUser = hasUserSelectedCountry,
                showDivider = !isLastStateInList
            )
          }
    }
  }

  override fun layoutResId() = R.layout.list_selectstate_state_view

  override fun render(holder: BindingViewHolder, subject: Subject<StateClicked>) {
    val binding = holder.binding as ListSelectstateStateViewBinding

    binding.stateRadioButton.apply {
      text = state.displayName
      isChecked = isStateSelectedByUser
      setOnClickListener { subject.onNext(StateClicked(state)) }
    }

    binding.divider.isInvisible = !showDivider
  }

  data class StateClicked(val state: State)

  class SelectableStateItemDiffCallback : DiffUtil.ItemCallback<SelectableStateItem>() {

    override fun areItemsTheSame(
        oldItem: SelectableStateItem,
        newItem: SelectableStateItem
    ): Boolean {
      return oldItem.state.displayName == newItem.state.displayName
    }

    override fun areContentsTheSame(
        oldItem: SelectableStateItem,
        newItem: SelectableStateItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
