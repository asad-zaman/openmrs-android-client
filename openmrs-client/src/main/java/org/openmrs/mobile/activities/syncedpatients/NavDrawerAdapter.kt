package org.openmrs.mobile.activities.syncedpatients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.openmrs.mobile.R
import org.openmrs.mobile.base.BaseViewHolder
import org.openmrs.mobile.listeners.ItemClickListener
import org.openmrs.mobile.models.NavDrawerItem

class NavDrawerAdapter(var itemClickListener: ItemClickListener, var navItems: List<NavDrawerItem>):
        RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): BaseViewHolder {
        return NavDrawerHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_nav_drawer_item, parent, false))
    }

    override fun getItemCount(): Int {
        return navItems?.size!!
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBindView(navItems[position])
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClicked(navItems[position])
        }
    }

    fun updateModuleItems(modules: List<NavDrawerItem>) {
        navItems = modules
        notifyDataSetChanged()
    }
}