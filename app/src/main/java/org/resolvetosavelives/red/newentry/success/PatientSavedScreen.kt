package org.resolvetosavelives.red.newentry.success

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.home.HomeScreen
import org.resolvetosavelives.red.router.screen.RouterDirection
import org.resolvetosavelives.red.router.screen.ScreenRouter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PatientSavedScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSavedScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.timer(1_500, TimeUnit.MILLISECONDS, mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe({ screenRouter.clearHistoryAndPush(HomeScreen.KEY, RouterDirection.BACKWARD) })
  }
}
