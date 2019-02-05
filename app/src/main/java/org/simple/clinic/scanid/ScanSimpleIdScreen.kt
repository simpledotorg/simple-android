package org.simple.clinic.scanid

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import javax.inject.Inject

class ScanSimpleIdScreen(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
  }
}
