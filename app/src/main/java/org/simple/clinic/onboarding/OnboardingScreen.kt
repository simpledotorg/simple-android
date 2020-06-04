package org.simple.clinic.onboarding

import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.screen_onboarding.view.*
import org.simple.clinic.R
import org.simple.clinic.util.Truss

class OnboardingScreen(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    setIntroTextViews()
  }

  private fun setIntroTextViews() {
    val introOneFormattedString = Truss()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Grey1))
        .append(context.getString(R.string.screenonboarding_intro_1))
        .popSpan()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_1_hypertension))
        .popSpan()
        .build()

    introOneTextView.text = introOneFormattedString

    val introTwoFormattedString = Truss()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Grey1))
        .append(context.getString(R.string.screenonboarding_intro_2))
        .popSpan()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_2_bp))
        .popSpan()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Grey1))
        .append(context.getString(R.string.screenonboarding_intro_2_and))
        .popSpan()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_2_medicines))
        .popSpan()
        .build()

    introTwoTextView.text = introTwoFormattedString

    val introThreeFormattedString = Truss()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Grey1))
        .append(context.getString(R.string.screenonboarding_intro_3))
        .popSpan()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_3_reminder))
        .popSpan()
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Grey1))
        .append(context.getString(R.string.screenonboarding_intro_3_visits))
        .popSpan()
        .build()

    introThreeTextView.text = introThreeFormattedString
  }
}
