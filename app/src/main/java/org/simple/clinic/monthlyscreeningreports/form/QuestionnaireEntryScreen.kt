package org.simple.clinic.monthlyscreeningreports.form

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenQuestionnaireEntryFormBinding
import org.simple.clinic.di.injector
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.questionnaire.component.ViewGroupComponentData
import org.simple.clinic.monthlyscreeningreports.form.epoxy.controller.QuestionnaireEntryFormController
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class QuestionnaireEntryScreen : BaseScreen<
    QuestionnaireEntryScreen.Key,
    ScreenQuestionnaireEntryFormBinding,
    QuestionnaireEntryModel,
    QuestionnaireEntryEvent,
    QuestionnaireEntryEffect,
    QuestionnaireEntryViewEffect>(),
    QuestionnaireEntryUi,
    HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var effectHandlerFactory: QuestionnaireEntryEffectHandler.Factory

  private val backButton
    get() = binding.backButton

  private val facilityTextView
    get() = binding.facilityTextView

  private val questionnaireFormRecyclerView
    get() = binding.questionnaireFormRecyclerView

  private val hotEvents = PublishSubject.create<QuestionnaireEntryEvent>()
  private val hardwareBackClicks = PublishSubject.create<Unit>()

  private val questionnaireFormController: QuestionnaireEntryFormController by lazy { QuestionnaireEntryFormController() }

  override fun defaultModel() = QuestionnaireEntryModel.default()

  override fun createInit() = QuestionnaireEntryInit(screenKey.questionnaireType)

  override fun createUpdate() = QuestionnaireEntryUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<QuestionnaireEntryViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = QuestionnaireEntryViewEffectHandler(this)

  override fun events(): Observable<QuestionnaireEntryEvent> {
    return Observable
        .mergeArray(
            backClicks(),
            hotEvents
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = QuestionnaireEntryUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenQuestionnaireEntryFormBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initRecyclerView()
  }

  @Parcelize
  data class Key(
      val questionnaireType: QuestionnaireType,
      override val analyticsName: String = "$questionnaireType questionnaire entry form"
  ) : ScreenKey() {

    override fun instantiateFragment() = QuestionnaireEntryScreen()
  }

  override fun setFacility(facilityName: String) {
    facilityTextView.text = facilityName
  }

  override fun displayQuestionnaireFormLayout(layout: BaseComponentData) {
    if (layout is ViewGroupComponentData) {
      questionnaireFormController.setData(layout.children)
    }
  }

  private fun initRecyclerView() {
    questionnaireFormRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      setController(questionnaireFormController)
    }
  }

  private fun backClicks(): Observable<UiEvent> {
    return backButton.clicks()
        .mergeWith(hardwareBackClicks)
        .map {
          QuestionnaireEntryBackClicked
        }
  }

  override fun showProgress() {
  }

  override fun hideProgress() {
  }

  override fun goBack() {
    router.pop()
  }

  override fun showUnsavedChangesWarningDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.monthly_screening_reports_unsaved_changes_title)
        .setMessage(R.string.monthly_screening_reports_unsaved_changes_text)
        .setPositiveButton(R.string.monthly_screening_reports_stay_title, null)
        .setNegativeButton(R.string.monthly_screening_reports_leave_page_title) { _, _ ->
          hotEvents.onNext(UnsavedChangesWarningLeavePageClicked)
        }
        .show()
  }

  interface Injector {
    fun inject(target: QuestionnaireEntryScreen)
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(Unit)
    return true
  }
}
