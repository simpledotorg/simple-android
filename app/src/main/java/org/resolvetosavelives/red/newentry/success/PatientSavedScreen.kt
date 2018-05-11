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
import java.util.concurrent.TimeUnit

class PatientSavedScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSavedScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    Observable.timer(1_500, TimeUnit.MILLISECONDS, mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe({ TheActivity.screenRouter().clearHistoryAndPush(HomeScreen.KEY, RouterDirection.BACKWARD) })
  }
}
