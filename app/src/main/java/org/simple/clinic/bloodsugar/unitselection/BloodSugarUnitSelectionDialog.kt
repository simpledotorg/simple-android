package org.simple.clinic.bloodsugar.unitselection

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.databinding.DialogBloodsugarSelectionunitBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.widgets.ScreenDestroyed
import javax.inject.Inject

class BloodSugarUnitSelectionDialog : BaseDialog<
    BloodSugarUnitSelectionDialog.Key,
    DialogBloodsugarSelectionunitBinding,
    BloodSugarUnitSelectionModel,
    BloodSugarUnitSelectionEvent,
    BloodSugarUnitSelectionEffect,
    Nothing>(), BloodSugarUnitSelectionUiActions {

  private lateinit var layout: View

  private val bloodSugarUnitGroup
    get() = binding.bloodSugarUnitGroup

  @Inject
  lateinit var effectHandlerFactory: BloodSugarUnitSelectionEffectHandler.Factory

  companion object {

    private const val FRAGMENT_TAG = "blood_sugar_unit_selection_tag"
    private const val KEY_UNIT_PREF = "bloodSugarUnitSelection"

    fun show(fragmentManager: FragmentManager, bloodSugarUnitPreference: BloodSugarUnitPreference) {
      val existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val arguments = Bundle()
      arguments.putSerializable(KEY_UNIT_PREF, bloodSugarUnitPreference)

      val fragment = BloodSugarUnitSelectionDialog()
      fragment.arguments = arguments

      fragmentManager
          .beginTransaction()
          .add(fragment, FRAGMENT_TAG)
          .commitNowAllowingStateLoss()
    }
  }

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = DialogBloodsugarSelectionunitBinding.inflate(layoutInflater, container, false)

  override fun defaultModel() = BloodSugarUnitSelectionModel.create(screenKey.bloodSugarUnitPreference)

  override fun createUpdate() = BloodSugarUnitSelectionUpdate()

  override fun createInit() = BloodSugarUnitSelectionInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Nothing>) = effectHandlerFactory.create(this).build()

  override fun events(): Observable<BloodSugarUnitSelectionEvent> {
    val doneButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    return Observable
        .merge<BloodSugarUnitSelectionEvent?> {
          doneClicks(doneButton)
          radioButtonClicks()
        }
        .cast()
  }

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()

  private fun doneClicks(doneButton: Button): Observable<BloodSugarUnitSelectionEvent> {

    val radioIdToBloodSugarUnits = mapOf(
        R.id.bloodSugarUnitMg to BloodSugarUnitPreference.Mg,
        R.id.bloodSugarUnitMmol to BloodSugarUnitPreference.Mmol
    )

    return doneButton
        .clicks()
        .map {
          val bloodSugarUnitSelectionValue = radioIdToBloodSugarUnits.getValue(bloodSugarUnitGroup.checkedRadioButtonId)
          DoneClicked(bloodSugarUnitSelection = bloodSugarUnitSelectionValue)
        }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<BloodSugarUnitSelectionDialogInjector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.blood_sugar_unit_selection_choose)
        .setView(layout)
        .setPositiveButton(R.string.blood_sugar_unit_selection_done, null)
        .create()
  }

  private fun radioButtonClicks(): Observable<BloodSugarUnitSelectionEvent> {
    return bloodSugarUnitGroup
        .checkedChanges()
        .map { checkedId ->
          when (checkedId) {
            R.id.bloodSugarUnitMg -> SaveBloodSugarUnitPreference(BloodSugarUnitPreference.Mg)
            R.id.bloodSugarUnitMmol -> SaveBloodSugarUnitPreference(BloodSugarUnitPreference.Mmol)
            else -> SaveBloodSugarUnitPreference(BloodSugarUnitPreference.Mg)
          }
        }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  override fun closeDialog() {
    dismiss()
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
