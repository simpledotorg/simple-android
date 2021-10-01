package org.simple.clinic.widgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

class PagingItemAdapter<I : PagingItemAdapter.Item<E>, E>(
    diffCallback: DiffUtil.ItemCallback<I>,
    private val bindings: BindingsCallback,
    private val placeHolderBinding: Pair<Int, (layoutInflater: LayoutInflater, parent: ViewGroup) -> ViewBinding>? = null
) : PagingDataAdapter<I, BindingViewHolder>(diffCallback) {

  private val eventSubject = PublishSubject.create<E>()
  val itemEvents: Observable<E> = eventSubject.hide()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
    val context = parent.context
    val layoutInflater = LayoutInflater.from(context)

    val bindingFactory = when {
      bindings.containsKey(viewType) -> bindings.getValue(viewType)
      placeHolderBinding?.first == viewType -> placeHolderBinding.second
      else -> throw IllegalArgumentException("Unknown view type: ${resourceNameForId(context.resources, viewType)}")
    }

    return BindingViewHolder(binding = bindingFactory.invoke(layoutInflater, parent))
  }

  override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
    getItem(position)?.render(holder, eventSubject)
  }

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return item?.layoutResId()
        ?: placeHolderBinding?.first
        ?: throw NullPointerException("Failed to get item at position: $position")
  }

  interface Item<E> {

    @LayoutRes
    fun layoutResId(): Int

    fun render(holder: BindingViewHolder, subject: Subject<E>)
  }
}
