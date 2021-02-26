package com.hypertrack.android.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

abstract class BaseAdapter<T : Any, VH : BaseAdapter.BaseVh<T>> : RecyclerView.Adapter<VH>() {

    var onItemClickListener: ((T) -> Unit)? = null

    val items: MutableList<T> = mutableListOf<T>()

    fun deleteItem(item: T) {
        items.remove(item)
        notifyDataSetChanged()
//        notifyItemRemoved()
    }

    fun updateItems(_items: List<T>) {
        items.clear()
        addItemsAndUpdate(_items)
    }

    fun addItemsAndUpdate(_items: List<T>) {
        items.addAll(_items)
        notifyDataSetChanged()
    }

    abstract val itemLayoutResource: Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return createViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        itemLayoutResource,
                        parent,
                        false
                )
        ) { position ->
            onItemClickListener?.invoke(items[position])
        }
    }

    open fun createViewHolder(view: View, baseClickListener: ((Int) -> Unit)): VH {
        throw NotImplementedError("createViewHolder")
//        return BaseVh<T>(
//            view,
//            baseClickListener
//        ) as VH
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    open class BaseVh<R : Any>(view: View, clickListener: ((Int) -> Unit)) :
            RecyclerView.ViewHolder(view) {

        private lateinit var item: R

        init {
            view.setOnClickListener {
                clickListener.invoke(adapterPosition)
            }
        }

        open fun bind(item: R) {

        }
    }

    open inner class BaseContainerVh<R : Any>(override val containerView: View, listener: (Int) -> Unit) : BaseVh<R>(containerView, listener),
            LayoutContainer {
    }

}

