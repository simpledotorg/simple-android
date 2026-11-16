package org.simple.clinic.confirmremoveid

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.parcelize.Parcelize
import org.simple.clinic.databinding.SheetConfirmRemoveIdBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import javax.inject.Inject

class ConfirmRemoveIdSheet : BaseBottomSheet<
    ConfirmRemoveIdSheet.Key,
    SheetConfirmRemoveIdBinding,
    ConfirmRemoveIdSheet.Model,
    Unit,
    Unit,
    Unit>() {

  @Inject
  lateinit var router: Router

  override fun defaultModel() = Model

  override fun bindView(
      inflater: LayoutInflater,
      container: ViewGroup?
  ) = SheetConfirmRemoveIdBinding.inflate(inflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.backButton.setOnClickListener { router.pop() }
    binding.removeButton.setOnClickListener { router.popWithResult(Succeeded(RemoveIdConfirmed)) }
  }

  @Parcelize
  data object Model : Parcelable

  @Parcelize
  data object RemoveIdConfirmed : Parcelable

  @Parcelize
  data class Key(
      override val analyticsName: String = "Confirm Remove ID Sheet"
  ) : ScreenKey() {

    override fun instantiateFragment() = ConfirmRemoveIdSheet()

    override val type = ScreenType.Modal
  }

  interface Injector {
    fun inject(target: ConfirmRemoveIdSheet)
  }
}
