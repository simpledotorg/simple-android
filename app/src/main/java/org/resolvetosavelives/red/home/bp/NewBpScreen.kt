package org.resolvetosavelives.red.home.bp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.search.PatientSearchByPhoneScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import javax.inject.Inject

open class NewBpScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = NewBpScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: NewBpScreenController

  private val phoneButton by bindView<View>(R.id.newbp_search_by_phone)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    RxView.clicks(phoneButton)
        .map { NewPatientClicked() }
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  fun openNewPatientScreen() {
    screenRouter.push(PatientSearchByPhoneScreen.KEY)
  }
}
