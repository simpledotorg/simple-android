package org.simple.clinic.monthlyscreeningreports.form.epoxy.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.simple.clinic.databinding.ViewQuestionnaireInputFieldBinding
import org.simple.clinic.questionnaire.component.InputFieldComponentData

class InputFieldAdapter : ListAdapter<InputFieldComponentData, RecyclerView.ViewHolder>(InputFieldDiff) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val binding =
        ViewQuestionnaireInputFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return InputFieldViewHolder(binding)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as InputFieldViewHolder).bind(getItem(position))
  }

  inner class InputFieldViewHolder(private val binding: ViewQuestionnaireInputFieldBinding) :
      RecyclerView.ViewHolder(binding.root) {
    fun bind(inputField: InputFieldComponentData) {
      binding.textInputLayout.hint = inputField.text
    }
  }
}

object InputFieldDiff : DiffUtil.ItemCallback<InputFieldComponentData>() {
  override fun areItemsTheSame(oldItem: InputFieldComponentData, newItem: InputFieldComponentData): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(oldItem: InputFieldComponentData, newItem: InputFieldComponentData): Boolean {
    return oldItem == newItem
  }
}

