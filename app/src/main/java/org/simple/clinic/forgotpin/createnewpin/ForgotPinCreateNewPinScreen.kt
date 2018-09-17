package org.simple.clinic.forgotpin.createnewpin

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class ForgotPinCreateNewPinScreen(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: ForgotPinCreateNewPinScreenController

  private val userNameTextView by bindView<TextView>(R.id.forgotpin_user_fullname)
  private val facilityNameTextView by bindView<TextView>(R.id.forgotpin_facility_name)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    screenCreates()
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { it.invoke(this) }
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  fun showUserName(name: String) {
    userNameTextView.text = name
  }

  fun showFacility(name: String) {
    facilityNameTextView.text = name
  }
}
