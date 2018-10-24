package org.simple.clinic.widgets

import android.content.Context
import android.support.v7.widget.AppCompatCheckBox
import android.util.AttributeSet

class CheckBoxWithSuppressibleListener(context: Context, attrs: AttributeSet) : AppCompatCheckBox(context, attrs) {

  private var listener: OnCheckedChangeListener? = null

  override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
    super.setOnCheckedChangeListener(listener)
    this.listener = listener
  }

  fun runWithoutListener(block: () -> Unit) {
    val copy = listener
    setOnCheckedChangeListener(null)

    block()
    setOnCheckedChangeListener(copy)
  }
}
