package org.simple.clinic.bloodsugar.unitselection

import android.app.Dialog
import android.app.Dialog.BUTTON_POSITIVE
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.databinding.DialogBloodsugarSelectionunitBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import javax.inject.Inject

class BloodSugarUnitSelectionDialog : BaseDialog<
    BloodSugarUnitSelectionDialog.Key,
    DialogBloodsugarSelectionunitBinding,
    BloodSugarUnitSelectionModel,
    BloodSugarUnitSelectionEvent,
    BloodSugarUnitSelectionEffect,
    BloodSugarUnitSelectionViewEffect>(), BloodSugarUnitSelectionUiActions {

  private val bloodSugarUnitGroup
    get() = binding.bloodSugarUnitGroup

  private val hotEvents: PublishSubject<BloodSugarUnitSelectionEvent> = PublishSubject.create()

  @Inject
  lateinit var effectHandlerFactory: BloodSugarUnitSelectionEffectHandler.Factory

  @Inject
  lateinit var router: Router

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = DialogBloodsugarSelectionunitBinding.inflate(layoutInflater, container, false)

  override fun defaultModel() = BloodSugarUnitSelectionModel.create(screenKey.bloodSugarUnitPreference)

  override fun createUpdate() = BloodSugarUnitSelectionUpdate()

  override fun createInit() = BloodSugarUnitSelectionInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<BloodSugarUnitSelectionViewEffect>) = effectHandlerFactory.create(
      viewEffectsConsumer = viewEffectsConsumer
  ).build()

  override fun viewEffectHandler() = BloodSugarUnitSelectionViewEffectHandler(this)

  override fun events(): Observable<BloodSugarUnitSelectionEvent> = hotEvents.cast()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<BloodSugarUnitSelectionDialogInjector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.blood_sugar_unit_selection_choose)
        .setPositiveButton(R.string.blood_sugar_unit_selection_done, null)
        .create()
  }

  override fun onResume() {
    super.onResume()
    if (dialog != null) {
      val radioIdToBloodSugarUnits = mapOf(
          R.id.bloodSugarUnitMg to BloodSugarUnitPreference.Mg,
          R.id.bloodSugarUnitMmol to BloodSugarUnitPreference.Mmol
      )
      val alertDialog = (dialog as AlertDialog)
      alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
        val bloodSugarUnitSelectionValue = radioIdToBloodSugarUnits.getValue(bloodSugarUnitGroup.checkedRadioButtonId)
        hotEvents.onNext(DoneClicked(bloodSugarUnitSelectionValue))
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = super.onCreateView(inflater, container, savedInstanceState)
    (dialog as? AlertDialog)?.setView(view)
    return view
  }

  override fun closeDialog() {
    router.pop()
  }

  override fun prefillBloodSugarUnitSelection(blodSugarUnitPreference: BloodSugarUnitPreference) {
    when (blodSugarUnitPreference) {
      BloodSugarUnitPreference.Mg -> bloodSugarUnitGroup.check(R.id.bloodSugarUnitMg)
      BloodSugarUnitPreference.Mmol -> bloodSugarUnitGroup.check(R.id.bloodSugarUnitMmol)
    }
  }

  interface BloodSugarUnitSelectionDialogInjector {
    fun inject(target: BloodSugarUnitSelectionDialog)
  }

  @Parcelize
  data class Key(
      val bloodSugarUnitPreference: BloodSugarUnitPreference,
      override val type: ScreenType = ScreenType.Modal,
      override val analyticsName: String = "Blood Sugar Unit Selection Dialog"
  ) : ScreenKey() {

    override fun instantiateFragment() = BloodSugarUnitSelectionDialog()
  }
}
