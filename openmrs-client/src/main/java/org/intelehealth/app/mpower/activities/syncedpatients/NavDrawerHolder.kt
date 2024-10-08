package org.intelehealth.app.mpower.activities.syncedpatients

import org.intelehealth.app.mpower.base.BaseViewHolder
import org.intelehealth.app.mpower.databinding.ListNavDrawerItemBinding
import org.intelehealth.app.mpower.models.NavDrawerItem


class NavDrawerHolder(val navItemBinding: ListNavDrawerItemBinding): BaseViewHolder(navItemBinding.root) {

    override fun onBindView(dataItem: Any) {
        navItemBinding.navdrawer = dataItem as NavDrawerItem
        navItemBinding.executePendingBindings()
    }
}