package org.simple.clinic.text.style

import android.content.Context
import android.text.ParcelableSpan
import android.text.Spanned
import android.text.TextPaint
import android.text.style.TextAppearanceSpan
import androidx.annotation.StyleRes

/**
 * The platform [TextAppearanceSpan] does not support setting letter spacing for the spanned text.
 * This class extends it to add support for setting the letter spacing.
 *
 * **Note:** [TextAppearanceSpan] implements [ParcelableSpan], but that cannot be implemented
 * outside the framework. This means that the letter spacing read in this span will be lost if the
 * [Spanned] this span is applied to is parcelled across activities. Since we do not send these
 * spans across framework components, this should be fine.
 *
 * See this [post](https://medium.com/androiddevelopers/underspanding-spans-1b91008b97e4) for more
 * information.
 **/
class TextAppearanceWithLetterSpacingSpan(
    context: Context,
    @StyleRes textAppearanceResId: Int
) : TextAppearanceSpan(context, textAppearanceResId) {

  private val letterSpacing: Float

  init {
    val typedArray = context.obtainStyledAttributes(textAppearanceResId, intArrayOf(android.R.attr.letterSpacing))
    letterSpacing = typedArray.getFloat(0, 0F)
    typedArray.recycle()
  }

  override fun updateMeasureState(ds: TextPaint) {
    super.updateMeasureState(ds)
    ds.letterSpacing = letterSpacing
  }
}
