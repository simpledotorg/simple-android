package org.resolvetosavelives.red.newentry.clearbutton

import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.RedApp
import javax.inject.Inject

class ClearFieldImageButton(context: Context, attrs: AttributeSet) : AppCompatImageButton(context, attrs) {

  @Inject
  lateinit var controller: ClearFieldImageButtonController

  private var fieldId: Int
  private lateinit var field: EditText
  private var fieldOriginalPaddingEnd: Int = 0

  init {
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.ClearFieldImageButton)
    fieldId = attributes.getResourceId(R.styleable.ClearFieldImageButton_cleareableField, 0)
    attributes.recycle()

    if (fieldId == 0) {
      throw AssertionError()
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    field.setPaddingRelative(field.paddingStart, field.paddingTop, fieldOriginalPaddingEnd + (width), field.paddingBottom)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    RedApp.appComponent.inject(this)

    try {
      field = (parent as ViewGroup).findViewById(fieldId)
    } catch (e: IllegalStateException) {
      throw NullPointerException("Couldn't find View (${resources.getResourceName(fieldId)}) inside immediate parent.")
    }

    fieldOriginalPaddingEnd = field.paddingEnd

    cleareableFieldTextChanges()
        .compose(controller)
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    setOnClickListener { field.text = null }
  }

  private fun cleareableFieldTextChanges() = RxTextView.textChanges(field)
      .map(CharSequence::toString)
      .map(::CleareableFieldTextChanged)

  fun setVisible(visible: Boolean) {
    visibility = when (visible) {
      true -> View.VISIBLE
      else -> View.GONE
    }
  }
}
