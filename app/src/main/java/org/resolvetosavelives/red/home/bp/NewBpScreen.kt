package org.resolvetosavelives.red.home.bp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.search.PatientSearchByMobileScreen
import timber.log.Timber

class NewBpScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = NewBpScreenKey()
  }

  private val mobileButton by bindView<View>(R.id.newbp_search_by_mobile)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    mobileButton.setOnClickListener({
      Timber.i("Going to patient mobile entry")
      TheActivity.screenRouter().push(PatientSearchByMobileScreen.KEY)
    })
  }
}
