package org.simple.clinic.teleconsultlog.success

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_teleconsult_success.view.*
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.Patient
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class TeleConsultSuccessScreen(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet), TeleConsultSuccessScreenUiActions {

  @Inject
  lateinit var effectHandler: TeleConsultSuccessEffectHandler.Factory

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val events: Observable<TeleConsultSuccessEvent> by unsafeLazy {
    Observable
        .merge(
            yesClicks(),
            noClicks()
        )
  }

  private
  val mobiusDelegate: MobiusDelegate<TeleConsultSuccessModel, TeleConsultSuccessEvent, TeleConsultSuccessEffect> by unsafeLazy {
    val screenKey = screenRouter.key<TeleConsultSuccessScreenKey>(this)
    MobiusDelegate.forView(
        events = events,
        defaultModel = TeleConsultSuccessModel.create(screenKey.patientUuid),
        init = TeleConsultSuccessInit(),
        update = TeleConsultSuccessUpdate(),
        effectHandler = effectHandler.create(this).build()
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)
    backClicks()
  }

  private fun noClicks() = prescriptionNoButton
      .clicks()
      .map { NoPrescriptionClicked }

  private fun yesClicks() = prescriptionYesButton
      .clicks()
      .map { YesPrescriptionClicked }

  private fun backClicks() {
    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mobiusDelegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    mobiusDelegate.stop()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return mobiusDelegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(mobiusDelegate.onRestoreInstanceState(state))
  }

  override fun goToHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey(), direction = BACKWARD)
  }

  override fun goToPrescriptionScreen(patient: Patient) {
  }

  interface Injector {
    fun inject(target: TeleConsultSuccessScreen)
  }

}
