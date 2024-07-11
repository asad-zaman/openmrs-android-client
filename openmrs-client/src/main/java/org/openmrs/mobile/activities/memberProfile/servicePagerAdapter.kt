package org.openmrs.mobile.activities.memberProfile

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.ApplicationConstants.MemberProfileTabs.SERVICE_FORM
import com.openmrs.android_sdk.utilities.ApplicationConstants.MemberProfileTabs.SERVICE_HISTORY
import org.openmrs.mobile.R

class ServicePagerAdapter(private val fm: FragmentManager,
                                   private val context: Context,
                                   private val mPatientId: String
) : FragmentPagerAdapter(fm) {

    private val registeredFragments = SparseArray<Fragment>()

    override fun getItem(position: Int): Fragment {
        return when (position) {
            SERVICE_FORM -> ServiceFormFragment.newInstance(mPatientId)
            SERVICE_HISTORY -> ServiceHistoryFragment.newInstance(mPatientId)
            else -> throw IllegalStateException()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            SERVICE_FORM -> context.getString(R.string.service_form_title)
            SERVICE_HISTORY -> context.getString(R.string.service_history_title)
            else -> super.getPageTitle(position)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    override fun getCount(): Int = ApplicationConstants.MemberProfileTabs.TAB_COUNT
}