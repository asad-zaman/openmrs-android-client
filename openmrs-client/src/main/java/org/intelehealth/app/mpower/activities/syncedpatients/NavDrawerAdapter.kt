package org.intelehealth.app.mpower.activities.syncedpatients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.base.BaseViewHolder
import org.intelehealth.app.mpower.listeners.ItemClickListener
import org.intelehealth.app.mpower.models.NavDrawerItem

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