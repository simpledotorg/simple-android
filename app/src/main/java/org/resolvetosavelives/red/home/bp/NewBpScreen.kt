package org.resolvetosavelives.red.home.bp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.search.PatientSearchByMobileScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import timber.log.Timber
import javax.inject.Inject

class NewBpScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = NewBpScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val mobileButton by bindView<View>(R.id.newbp_search_by_mobile)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    mobileButton.setOnClickListener({
      Timber.i("Going to patient mobile entry")
      screenRouter.push(PatientSearchByMobileScreen.KEY)
    })
  }
}
