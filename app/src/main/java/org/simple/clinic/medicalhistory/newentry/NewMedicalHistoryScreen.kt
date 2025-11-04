package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Features
import org.simple.clinic.medicalhistory.SelectDiagnosisErrorDialog
import org.simple.clinic.medicalhistory.SelectOngoingDiabetesTreatmentErrorDialog
import org.simple.clinic.medicalhistory.SelectOngoingHypertensionTreatmentErrorDialog
import org.simple.clinic.medicalhistory.ui.NewMedicalHistoryUi
import org.simple.clinic.mobius.DisposableViewEffect
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class NewMedicalHistoryScreen : Fragment(), NewMedicalHistoryUiActions, HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: NewMedicalHistoryEffectHandler.Factory

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var newMedicalHistoryEffectHandler: NewMedicalHistoryEffectHandler.Factory

  private val viewEffectHandler by unsafeLazy { NewMedicalHistoryViewEffectHandler(this) }

  private val viewModel by viewModels<NewMedicalHistoryViewModel>(
      factoryProducer = {
        NewMedicalHistoryViewModel.factory(
            country = country,
            features = features,
            effectHandlerFactory = effectHandlerFactory)
      }
  )

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        viewModel.viewEffects.DisposableViewEffect(viewEffectHandler::handle)

        val model by viewModel.models.observeAsState()
        model?.let {
          NewMedicalHistoryUi(
              model = it,
              navigationIconClick = { onBackPressed() },
              onNextClick = {
                viewModel.dispatch(SaveMedicalHistoryClicked())
              }
          ) { question, answer ->
            viewModel.dispatch(NewMedicalHistoryAnswerToggled(question, answer))
          }
        }
      }
    }
  }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    router.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewNewPatient, Instant.now(utcClock)))
  }

  override fun showOngoingDiabetesTreatmentErrorDialog() {
    SelectOngoingDiabetesTreatmentErrorDialog.show(fragmentManager = activity.supportFragmentManager)
  }

  override fun goBack() {
    router.pop()
  }

  override fun onBackPressed(): Boolean {
    viewModel.dispatch(BackClicked)
    return true
  }

  interface Injector {
    fun inject(target: NewMedicalHistoryScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "New Medical History Entry"
  ) : ScreenKey() {
    override fun instantiateFragment() = NewMedicalHistoryScreen()
  }
}
