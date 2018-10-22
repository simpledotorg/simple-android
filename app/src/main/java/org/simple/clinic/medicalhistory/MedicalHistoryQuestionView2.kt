package org.simple.clinic.medicalhistory

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.setHorizontalPadding

@SuppressLint("ClickableViewAccessibility")
class MedicalHistoryQuestionView2(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  private val labelTextView by bindView<TextView>(R.id.newmedicalhistory_item_label)
  private val divider by bindView<View>(R.id.newmedicalhistory_item_divider)
  private val checkYes by bindView<CompoundButton>(R.id.newmedicalhistory_item_yes)
  private val checkNo by bindView<CompoundButton>(R.id.newmedicalhistory_item_no)

  lateinit var question: MedicalHistoryQuestion

//  var isChecked: Boolean
//    get() = checkYes.isChecked
//    set(value) {
//        checkYes.isChecked = value
//        checkNo.isChecked = value.not()
//    }

  var isChecked: Boolean
    get() = true
    set(value) {
//        checkYes.isChecked = value
//        checkNo.isChecked = value.not()
    }

  init {
    LayoutInflater.from(context).inflate(R.layout.list_medical_history_question2, this, true)

    val color: (Int) -> Int = { colorRes -> ContextCompat.getColor(context, colorRes) }
    val setCheckedState: (CompoundButton, Boolean) -> Unit = { button, isChecked ->
      button.apply {
        when {
          isChecked -> {
            setTextColor(color(R.color.white100))
            setCompoundDrawableStart(R.drawable.ic_done_16dp)
            setHorizontalPadding(R.dimen.medicalhistory_unselected_padding)
          }
          else -> {
            setTextColor(color(R.color.blue1))
            setCompoundDrawableStart(null)
            setHorizontalPadding(R.dimen.medicalhistory_selected_padding)
          }
        }
      }
    }

    val listener = { buttonView: CompoundButton, isChecked: Boolean ->
      setCheckedState(buttonView,isChecked)
    }
    checkYes.setOnCheckedChangeListener(listener)
    checkNo.setOnCheckedChangeListener(listener)

    listener(checkYes, checkYes.isChecked)
    listener(checkNo, checkYes.isChecked)
  }

  fun setOnCheckedChangeListener(listener: (View, Boolean) -> Unit) {
   // checkBox.setOnCheckedChangeListener(listener)
  }

  fun checkedChanges(): Observable<Boolean> {
  //  return RxCompoundButton.checkedChanges(checkYes)
    return Observable.empty<Boolean>()
  }

  fun hideDivider() {
    divider.visibility = View.GONE
  }

  fun render(question: MedicalHistoryQuestion) {
    this.question = question
    labelTextView.setText(question.questionRes)
  }
}
