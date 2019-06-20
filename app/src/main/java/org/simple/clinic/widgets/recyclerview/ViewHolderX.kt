package org.simple.clinic.widgets.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

class ViewHolderX(
    override val containerView: View
) : LayoutContainer, RecyclerView.ViewHolder(containerView)
