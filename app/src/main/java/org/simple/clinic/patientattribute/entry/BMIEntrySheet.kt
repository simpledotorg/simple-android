package org.simple.clinic.patientattribute.entry

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.toObservable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetBmiReadingBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class BMIEntrySheet : BaseBottomSheet<
    BMIEntrySheet.Key,
    SheetBmiReadingBinding,
    BMIEntryModel,
    BMIEntryEvent,
    BMIEntryEffect,
    BMIEntryViewEffect>(),
    BMIEntryUi {

    @Inject
    lateinit var effectHandlerFactory: BMIEntryEffectHandler.Factory

    @Inject
    lateinit var router: Router

    private val additionalEvents = DeferredEventSource<BMIEntryEvent>()

    private val rootLayout
        get() = binding.rootLayout

    private val backImageButton
        get() = binding.backImageButton

    private val heightEditText
        get() = binding.heightEditText

    private val weightEditText
        get() = binding.weightEditText

    private val bmiTextView
        get() = binding.bmiTextView

    override fun defaultModel() = BMIEntryModel.default(
        patientUUID = screenKey.patientId,
    )

    override fun uiRenderer() = BMIEntryUiRenderer(this)

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        SheetBmiReadingBinding.inflate(layoutInflater, container, false)

    override fun createUpdate() = BMIEntryUpdate()

    override fun createEffectHandler(viewEffectsConsumer: Consumer<BMIEntryViewEffect>) =
        effectHandlerFactory.create(this).build()

    override fun additionalEventSources() = listOf(
        additionalEvents
    )

    override fun events() = Observable
        .mergeArray(
            heightChanges(),
            weightChanges(),
            weightBackspaceClicks(),
            imeDoneClicks(),
            hardwareBackPresses(),
            backButtonClicks(),
        )
        .compose(ReportAnalyticsEvents())
        .cast<BMIEntryEvent>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        context.injector<Injector>().inject(this)
    }

    private fun backButtonClicks(): Observable<UiEvent> {
        return backImageButton
            .clicks()
            .map { BackPressed }
    }

    private fun heightChanges() = heightEditText
        .textChanges()
        .map(CharSequence::toString)
        .map(::HeightChanged)

    private fun weightChanges() = weightEditText
        .textChanges()
        .map(CharSequence::toString)
        .map(::WeightChanged)

    private fun weightBackspaceClicks(): Observable<UiEvent> {
        return weightEditText
            .backspaceClicks
            .map { WeightBackspaceClicked }
    }

    private fun imeDoneClicks(): Observable<SaveClicked> {
        return listOf(heightEditText, weightEditText)
            .map { it.editorActions { actionId -> actionId == EditorInfo.IME_ACTION_DONE } }
            .toObservable()
            .flatMap { it }
            .map { SaveClicked }
    }

    private fun hardwareBackPresses(): Observable<UiEvent> {
        return Observable.create { emitter ->
            val interceptor = {
                emitter.onNext(BackPressed)
            }
            emitter.setCancellable { rootLayout.backKeyPressInterceptor = null }
            rootLayout.backKeyPressInterceptor = interceptor
        }
    }

    override fun closeSheet() {
        router.popWithResult(Succeeded(BMIAdded))
    }

    override fun changeFocusToHeight() {
        heightEditText.requestFocus()
    }

    override fun changeFocusToWeight() {
        weightEditText.requestFocus()
    }

    override fun showBMI(bmi: String) {
        bmiTextView.text = getString(R.string.bmi_x, bmi)
        bmiTextView.visibility = View.VISIBLE
    }

    override fun hideBMI() {
        bmiTextView.visibility = View.GONE
    }

    @Parcelize
    data class Key(
        val patientId: UUID,
    ) : ScreenKey() {

        override val analyticsName = "BMI Entry Sheet"

        override val type = ScreenType.Modal

        override fun instantiateFragment() = BMIEntrySheet()
    }

    interface Injector {
        fun inject(target: BMIEntrySheet)
    }

    @Parcelize
    object BMIAdded : Parcelable
}

