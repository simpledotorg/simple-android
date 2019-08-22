package org.simple.clinic.shortcodesearchresult

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.screen_shortcode_search_result.view.*
import org.simple.clinic.R
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import javax.inject.Inject

class ShortCodeSearchResultScreen(context: Context, attributes: AttributeSet) : RelativeLayout(context, attributes) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  override fun onFinishInflate() {
    super.onFinishInflate()
    setupToolBar()
  }

  private fun setupToolBar() {
    val screenKey = screenRouter.key<ShortCodeSearchResultScreenKey>(this)
    val shortCode = screenKey.shortCode

    // This is guaranteed to be exactly 7 characters in length.
    val prefix = shortCode.substring(0, 3)
    val suffix = shortCode.substring(3)

    val formattedShortCode = "$prefix${Unicode.nonBreakingSpace}$suffix"

    val textSpacingSpan = TextAppearanceWithLetterSpacingSpan(context, R.style.Clinic_V2_TextAppearance_Body0Left_NumericBold_White100)

    toolBar.title = Truss()
        .pushSpan(textSpacingSpan)
        .append(formattedShortCode)
        .popSpan()
        .build()
  }
}
