package org.simple.clinic.deniedaccess

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_access_denied.view.*
import kotlinx.android.synthetic.main.screen_patient_summary.view.*
import org.simple.clinic.main.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import javax.inject.Inject

class AccessDeniedScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    logoutButton.setOnClickListener {
      Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
    }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  fun setUserFullName(fullName: String) {
    fullNameTextView.text = fullName
  }

  fun exitApp() {
    activity.finish()
  }
}
