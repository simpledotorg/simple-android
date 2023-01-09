package org.simple.clinic.monthlyReports.questionnaire.entry

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spotify.mobius.functions.Consumer
import kotlinx.parcelize.Parcelize
import org.simple.clinic.databinding.ScreenQuestionnaireEntryFormBinding
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType
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
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var effectHandlerFactory: QuestionnaireEntryEffectHandler.Factory

  override fun defaultModel() = QuestionnaireEntryModel.default()

  override fun createInit() = QuestionnaireEntryInit(screenKey.questionnaireType)

  override fun createUpdate() = QuestionnaireEntryUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<QuestionnaireEntryViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun uiRenderer() = QuestionnaireEntryUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenQuestionnaireEntryFormBinding.inflate(layoutInflater, container, false)

  @Parcelize
  data class Key(
      val questionnaireType: QuestionnaireType,
      override val analyticsName: String = "$questionnaireType questionnaire entry form"
  ) : ScreenKey() {

    override fun instantiateFragment() = QuestionnaireEntryScreen()
  }

  override fun showProgress() {
  }

  override fun hideProgress() {
  }

  interface Injector {
    fun inject(target: QuestionnaireEntryScreen)
  }
}
