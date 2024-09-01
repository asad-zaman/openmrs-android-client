package org.openmrs.mobile.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BaseRecyclerViewAdapter<T>(
    private val layoutId: Int,
    private val bind: (View, T, Int) -> Unit,
    private val clickListener: ((T) -> Unit)? = null
) : RecyclerView.Adapter<BaseRecyclerViewAdapter.GenericViewHolder<T>>() {

    private var items: List<T> = listOf()

    class GenericViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: T, bind: (View, T, Int) -> Unit, position: Int) {
            bind(itemView, item, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return GenericViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        val item = items[position]
        holder.bind(item, bind, position)
        holder.itemView.setOnClickListener {
            clickListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }
}