package org.simple.clinic.monthlyReports.questionnaire.entry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spotify.mobius.functions.Consumer
import kotlinx.parcelize.Parcelize
import org.simple.clinic.databinding.ScreenQuestionnaireEntryFormBinding
import org.simple.clinic.di.injector
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType
import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponentData
import org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.controller.QuestionnaireEntryFormController
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class QuestionnaireEntryScreen : BaseScreen<
    QuestionnaireEntryScreen.Key,
    ScreenQuestionnaireEntryFormBinding,
    QuestionnaireEntryModel,
    QuestionnaireEntryEvent,
    QuestionnaireEntryEffect,
    QuestionnaireEntryViewEffect>(), QuestionnaireEntryUi {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var effectHandlerFactory: QuestionnaireEntryEffectHandler.Factory

  private val questionnaireFormToolbar
    get() = binding.questionnaireFormToolbar

  private val questionnaireFormRecyclerView
    get() = binding.questionnaireFormRecyclerView

  private val questionnaireFormController: QuestionnaireEntryFormController by lazy { QuestionnaireEntryFormController() }

  override fun defaultModel() = QuestionnaireEntryModel.default()

  override fun createInit() = QuestionnaireEntryInit(screenKey.questionnaireType)

  override fun createUpdate() = QuestionnaireEntryUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<QuestionnaireEntryViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = QuestionnaireEntryViewEffectHandler(this)

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
    questionnaireFormToolbar.setNavigationOnClickListener { router.pop() }
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
    questionnaireFormToolbar.title = facilityName
  }

  override fun displayQuestionnaireFormLayout(layout: BaseComponentData) {
    questionnaireFormController.questionnaireFormLayout = layout
  }

  private fun initRecyclerView() {
    questionnaireFormRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      setController(questionnaireFormController)
    }
  }


  override fun showProgress() {
  }

  override fun hideProgress() {
  }

  override fun goBack() {
  }

  interface Injector {
    fun inject(target: QuestionnaireEntryScreen)
  }
}
