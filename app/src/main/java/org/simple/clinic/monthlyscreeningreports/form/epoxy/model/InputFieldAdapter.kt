package org.simple.clinic.monthlyscreeningreports.form.epoxy.model

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.simple.clinic.databinding.ViewQuestionnaireInputFieldBinding
import org.simple.clinic.questionnaire.component.InputFieldComponentData
import org.simple.clinic.questionnaire.component.properties.Integer

class InputFieldAdapter(
    private val onTextChange: (Map<String, Any>) -> Unit
) : ListAdapter<InputFieldComponentData, RecyclerView.ViewHolder>(InputFieldDiff) {

  val values = mutableMapOf<String, Any>()

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
      binding.textEditText.inputType = if (inputField.type == Integer)
        InputType.TYPE_CLASS_NUMBER else
        InputType.TYPE_CLASS_TEXT

//      if (inputField.value != null) {
//        when (inputField.type) {
//          is Integer -> {
//            val value = try {
//              (inputField.value as Number).toInt()
//            } catch (ex: Exception) {
//              0
//            }
//            if (value > 0) {
//              binding.textEditText.setText(value.toString())
//            }
//          }
//          else -> {
//            binding.textEditText.setText(inputField.value as String)
//          }
//        }
//      }

      binding.textEditText.addTextChangedListener(object : TextWatcher {

        override fun afterTextChanged(s: Editable) {}

        override fun beforeTextChanged(
            s: CharSequence, start: Int,
            count: Int, after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence, start: Int,
            before: Int, count: Int
        ) {

          values[inputField.linkId] = when (inputField.type) {
            is Integer -> s.toString().toInt()
            else -> s.toString()
          }
          onTextChange.invoke(values)
        }
      })
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

