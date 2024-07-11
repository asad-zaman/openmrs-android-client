package org.openmrs.mobile.activities.syncedpatients

import org.openmrs.mobile.base.BaseViewHolder
import org.openmrs.mobile.databinding.ListNavDrawerItemBinding
import org.openmrs.mobile.models.NavDrawerItem


class NavDrawerHolder(val navItemBinding: ListNavDrawerItemBinding): BaseViewHolder(navItemBinding.root) {

    override fun onBindView(dataItem: Any) {
        navItemBinding.navdrawer = dataItem as NavDrawerItem
        navItemBinding.executePendingBindings()
    }
}