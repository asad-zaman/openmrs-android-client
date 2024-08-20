package org.openmrs.mobile.activities.memberProfile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.utilities.ApplicationConstants
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.ACBaseActivity
import org.openmrs.mobile.databinding.ActivityMemberProfileBinding


@AndroidEntryPoint
class MemberProfileActivity : ACBaseActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityMemberProfileBinding
    private val mViewModel: MemberProfileViewModel by viewModels()
    private lateinit var mPatient: Patient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_member_profile)
        mBinding.viewModel = mViewModel

        val patientString = this.intent.getStringExtra(ApplicationConstants.BundleKeys.PATIENT_ENTITY)
        mViewModel.patient = Gson().fromJson(patientString, Patient::class.java)

        observeData()
        initViewPager()
        mViewModel.populateProfileData()
        mViewModel.populateServiceHistory()

        supportActionBar?.let {
            it.elevation = 0f
            it.setDisplayHomeAsUpEnabled(true)
            it.title = mViewModel.patient.person.display
        }
    }

    private fun initViewPager() {
        val adapter = ServicePagerAdapter(supportFragmentManager, this, mViewModel.patient)
        with(mBinding) {
            if (org.openmrs.mobile.utilities.ThemeUtils.isDarkModeActivated()) tabProfile.setBackgroundColor(resources.getColor(
                org.openmrs.mobile.R.color.black_dark_mode))
            viewpagerProfile.offscreenPageLimit = adapter.count - 1
            viewpagerProfile.adapter = adapter
            tabProfile.setupWithViewPager(viewpagerProfile)
            viewpagerProfile.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {}
                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    private fun observeData() {
        mViewModel.rxReligion.observe(this, Observer {
            mBinding.tvMemberReligion.text = it
        })
        mViewModel.rxMaritalStatus.observe(this, Observer {
            mBinding.tvMemberMaritalStatus.text = it
        })
        mViewModel.rxBloodGroup.observe(this, Observer {
            mBinding.tvMemberBloodGroup.text = it
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.actionAddPatients -> {

            }
            android.R.id.home -> onBackPressed()
            else -> {
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.find_locally_and_add_patients_menu, menu)


        val searchMenuItem = menu.findItem(R.id.actionSearchLocal)
        val searchView = menu.findItem(R.id.actionSearchLocal).actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                return true
            }
        })
        return true
    }

    override fun onClick(v: View?) {
        val contentDashboard = mBinding.viewpagerProfile
        when (v?.id) {
        }
    }
}