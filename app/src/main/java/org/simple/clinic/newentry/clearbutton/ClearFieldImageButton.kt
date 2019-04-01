package org.simple.clinic.newentry.clearbutton

import android.content.Context
import androidx.appcompat.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.newentry.MultipleFocusChangeListeners
import org.simple.clinic.widgets.resourceNameForId
import javax.inject.Inject

class ClearFieldImageButton(context: Context, attrs: AttributeSet) : AppCompatImageButton(context, attrs) {

  @Inject
  lateinit var controller: ClearFieldImageButtonController

  private var fieldId: Int
  private var fieldName: String
  private lateinit var field: EditText
  private var fieldOriginalPaddingEnd: Int = 0

  init {
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.ClearFieldImageButton)
    fieldId = attributes.getResourceId(R.styleable.ClearFieldImageButton_cleareableField, 0)
    attributes.recycle()

    if (fieldId == 0) {
      throw AssertionError()
    }

    fieldName = resourceNameForId(resources, fieldId)
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    field.setPaddingRelative(field.paddingStart, field.paddingTop, fieldOriginalPaddingEnd + (width), field.paddingBottom)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if(isInEditMode) {
      return
    }
    ClinicApp.appComponent.inject(this)

    try {
      field = (parent as ViewGroup).findViewById(fieldId)
    } catch (e: IllegalStateException) {
      throw NullPointerException("Couldn't find View ($fieldName) inside immediate parent.")
    }

    fieldOriginalPaddingEnd = field.paddingEnd

    Observable.merge(cleareableFieldTextChanges(), cleareableFieldFocusChanges())
        .compose(controller)
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    setOnClickListener { field.text = null }
  }

  private fun cleareableFieldFocusChanges(): Observable<CleareableFieldFocusChanged> {
    return if (field is MultipleFocusChangeListeners) {
      (field as MultipleFocusChangeListeners)
          .focusChanges
          .map { hasFocus -> CleareableFieldFocusChanged(hasFocus, fieldName) }
    } else {
      RxView
          .focusChanges(field)
          .map { hasFocus -> CleareableFieldFocusChanged(hasFocus, fieldName) }
    }
  }

  private fun cleareableFieldTextChanges() = RxTextView.textChanges(field)
      .map(CharSequence::toString)
      .map { text -> CleareableFieldTextChanged(text, fieldName) }

  fun setVisible(visible: Boolean) {
    visibility = when (visible) {
      true -> View.VISIBLE
      else -> View.GONE
    }
  }
}
