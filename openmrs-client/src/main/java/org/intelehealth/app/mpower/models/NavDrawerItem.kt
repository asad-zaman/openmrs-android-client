package org.intelehealth.app.mpower.models

import android.content.Context
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.resources.Constants

class NavDrawerItem(
    var title: String,
    var icon: Int,
    var id: String
    ) {

    companion object {
        fun getNavDrawerItems(c : Context): List<NavDrawerItem> {

            val items: MutableList<NavDrawerItem> = ArrayList()
            items.add(NavDrawerItem(c.getString(R.string.drawer_member_profile), R.drawable.ic_person_grey_500_48dp, Constants.ITEM_MEMBER_PROFILE))
            items.add(NavDrawerItem(c.getString(R.string.drawer_find_patient), R.drawable.ic_add, Constants.ITEM_FIND_PATIENT))
            items.add(NavDrawerItem(c.getString(R.string.drawer_add_patient), R.drawable.ic_add, Constants.ITEM_ADD_PATIENT))
            items.add(NavDrawerItem(c.getString(R.string.drawer_add_member), R.drawable.ic_add, Constants.ITEM_ADD_MEMBER))
            items.add(NavDrawerItem(c.getString(R.string.drawer_find_member), R.drawable.ic_add, Constants.ITEM_FIND_MEMBER))
            items.add(NavDrawerItem(c.getString(R.string.drawer_referral_list), R.drawable.ic_add, Constants.ITEM_REFERRED_MEMBER_LIST))
            items.add(NavDrawerItem(c.getString(R.string.drawer_stock_management), R.drawable.ic_add, Constants.ITEM_STOCK_MANAGEMENT))
            items.add(NavDrawerItem(c.getString(R.string.drawer_active_visits), R.drawable.ic_add, Constants.ITEM_ACTIVE_VISITS))
            items.add(NavDrawerItem(c.getString(R.string.drawer_form_entry), R.drawable.ic_add, Constants.ITEM_FORM_ENTRY))
            items.add(NavDrawerItem(c.getString(R.string.drawer_manage_providers), R.drawable.ic_add, Constants.ITEM_MANAGE_PROVIDERS))

            return items
        }
    }

}