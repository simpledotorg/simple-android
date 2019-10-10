package org.simple.clinic.settings

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_settings.view.*
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SettingsScreen(
    context: Context,
    attributeSet: AttributeSet
) : LinearLayout(context, attributeSet), SettingsUi {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val uiRenderer: SettingsUiRenderer = SettingsUiRenderer(this)

  private val delegate: MobiusDelegate<SettingsModel, SettingsEvent, SettingsEffect> by unsafeLazy {
    MobiusDelegate(
        events = Observable.never(),
        defaultModel = SettingsModel.FETCHING_USER_DETAILS,
        init = SettingsInit(),
        update = SettingsUpdate(),
        effectHandler = SettingsEffectHandler.create(userSession, schedulersProvider),
        modelUpdateListener = uiRenderer::render,
        crashReporter = crashReporter
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    toolbar.setNavigationOnClickListener { screenRouter.pop() }

    delegate.prepare()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun displayUserDetails(name: String, phoneNumber: String) {
    userName.text = name
    userNumber.text = phoneNumber
  }
}
