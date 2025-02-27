package org.simple.clinic.summary.addcholesterol

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.widget.editorActions
import com.spotify.mobius.Update
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetCholesterolEntryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.textChanges
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID
import javax.inject.Inject

class CholesterolEntrySheet : BaseBottomSheet<
    CholesterolEntrySheet.Key,
    SheetCholesterolEntryBinding,
    CholesterolEntryModel,
    CholesterolEntryEvent,
    CholesterolEntryEffect,
    CholesterolEntryViewEffect>(), CholesterolEntryUi, CholesterolEntryUiActions {

  @Inject
  lateinit var cholesterolEntryEffectHandlerFactory: CholesterolEntryEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val rootLayout
    get() = binding.rootLayout

  private val cholesterolTextField
    get() = binding.cholesterolTextField

  private val cholesterolErrorTextView
    get() = binding.cholesterolErrorTextView

  private val progressIndicator
    get() = binding.progressLoader

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun defaultModel() = CholesterolEntryModel.create(
      patientUUID = screenKey.patientUuid,
  )

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) = SheetCholesterolEntryBinding
      .inflate(inflater, container, false)

  override fun createUpdate(): Update<CholesterolEntryModel, CholesterolEntryEvent, CholesterolEntryEffect> {
    return CholesterolEntryUpdate()
  }

  override fun createEffectHandler(viewEffectsConsumer: Consumer<CholesterolEntryViewEffect>): ObservableTransformer<CholesterolEntryEffect, CholesterolEntryEvent> {
    return cholesterolEntryEffectHandlerFactory.create(viewEffectsConsumer).build()
  }

  override fun viewEffectsHandler(): ViewEffectsHandler<CholesterolEntryViewEffect> {
    return CholesterolEntryViewEffectHandler(this)
  }

  override fun uiRenderer(): ViewRenderer<CholesterolEntryModel> {
    return CholesterolEntryUiRenderer(this)
  }

  override fun events() = Observable
      .mergeArray(
          hardwareBackPresses(),
          cholesterolTextChanges(),
          imeDoneClicks(),
      )
      .compose(ReportAnalyticsEvents())
      .cast<CholesterolEntryEvent>()


  override fun hideErrorMessage() {
    cholesterolErrorTextView.visibleOrGone(isVisible = false)
  }

  override fun dismissSheet() {
    router.popWithResult(Succeeded(CholesterolAdded))
  }

  override fun showReqMaxCholesterolError() {
    showCholesterolErrorMessage(getString(R.string.cholesterol_error_upper_limit))
  }

  override fun showReqMinCholesterolError() {
    showCholesterolErrorMessage(getString(R.string.cholesterol_error_lower_limit))
  }

  override fun showProgress() {
    progressIndicator.visibleOrGone(isVisible = true)
    cholesterolTextField.visibleOrGone(isVisible = false)
  }

  override fun hideProgress() {
    progressIndicator.visibleOrGone(isVisible = false)
    cholesterolTextField.visibleOrGone(isVisible = true)
  }

  private fun showCholesterolErrorMessage(message: String) {
    with(cholesterolErrorTextView) {
      text = message
      visibility = android.view.View.VISIBLE
    }
  }

  private fun hardwareBackPresses(): Observable<UiEvent> {
    return Observable.create { emitter ->
      val interceptor = {
        emitter.onNext(KeyboardClosed)
      }
      emitter.setCancellable { rootLayout.backKeyPressInterceptor = null }
      rootLayout.backKeyPressInterceptor = interceptor
    }
  }

  private fun cholesterolTextChanges() = cholesterolTextField
      .textChanges { CholesterolChanged(it.toFloatOrNull() ?: 0f) }

  private fun imeDoneClicks(): Observable<SaveClicked> {
    return cholesterolTextField
        .editorActions { actionId -> actionId == EditorInfo.IME_ACTION_DONE }
        .map { SaveClicked }
  }

  @Parcelize
  data class Key(
      val patientUuid: UUID,
      override val analyticsName: String = "Cholesterol Entry Sheet",
      override val type: ScreenType = ScreenType.Modal,
  ) : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return CholesterolEntrySheet()
    }
  }

  interface Injector {
    fun inject(target: CholesterolEntrySheet)
  }

  @Parcelize
  data object CholesterolAdded : Parcelable
}
