package org.simple.clinic.widgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

open class ItemAdapter<I : ItemAdapter.Item<E>, E>(
    diffCallback: DiffUtil.ItemCallback<I>,
    private val bindings: Map<Int, (layoutInflater: LayoutInflater, parent: ViewGroup) -> ViewBinding>
) : ListAdapter<I, BindingViewHolder>(diffCallback) {

  private val eventSubject: Subject<E> = PublishSubject.create<E>()

  val itemEvents: Observable<E> = eventSubject.hide()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)

    val binding = bindings.getValue(viewType)

    return BindingViewHolder(binding = binding.invoke(layoutInflater, parent))
  }

  override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
    getItem(position).render(holder, eventSubject)
  }

  override fun getItemViewType(position: Int): Int {
    return getItem(position).layoutResId()
  }

  interface Item<E> {

    @LayoutRes
    fun layoutResId(): Int

    fun render(holder: BindingViewHolder, subject: Subject<E>)
  }
}

