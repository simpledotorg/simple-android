package org.simple.clinic.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.res.use
import androidx.core.widget.TextViewCompat
import com.google.android.material.card.MaterialCardView
import org.simple.clinic.R
import org.simple.clinic.databinding.ViewPatientStatusBinding

class PatientStatusView(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

  private var binding: ViewPatientStatusBinding? = null

  private val statusTextView
    get() = binding!!.statusTextView

  init {
    val inflater = LayoutInflater.from(context)
    binding = ViewPatientStatusBinding.inflate(inflater, this)

    context.obtainStyledAttributes(attrs, R.styleable.PatientStatusView).use { typedArray ->
      val statusIconDrawable = typedArray.getDrawable(R.styleable.PatientStatusView_statusIcon)
      val statusText = typedArray.getString(R.styleable.PatientStatusView_statusText)
      val statusTextColor = typedArray.getColor(R.styleable.PatientStatusView_statusTextColor, 0)
      val statusIconColor = typedArray.getColor(R.styleable.PatientStatusView_statusIconTint, statusTextColor)
      val statusTextAppearance = typedArray.getResourceId(R.styleable.PatientStatusView_statusTextAppearance, R.style.TextAppearance_Simple_Headline6)

      statusTextView.text = statusText
      statusTextView.setTextColor(statusTextColor)
      statusTextView.setCompoundDrawableStart(statusIconDrawable)

      TextViewCompat.setCompoundDrawableTintList(statusTextView, ColorStateList.valueOf(statusIconColor))
      TextViewCompat.setTextAppearance(statusTextView, statusTextAppearance)
    }
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }
}
