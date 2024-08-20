/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.mobile.activities.syncedpatients

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.openmrs.android_sdk.library.OpenmrsAndroid
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.utilities.NetworkUtils
import com.openmrs.android_sdk.utilities.StringUtils.notEmpty
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_new_dashboard.view.*
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.ACBaseActivity
import org.openmrs.mobile.activities.activevisits.ActiveVisitsActivity
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity
import org.openmrs.mobile.activities.formentrypatientlist.FormEntryPatientListActivity
import org.openmrs.mobile.activities.lastviewedpatients.LastViewedPatientsActivity
import org.openmrs.mobile.activities.memberList.MemberListActivity
import org.openmrs.mobile.activities.memberProfile.MemberProfileActivity
import org.openmrs.mobile.activities.providermanagerdashboard.ProviderManagerDashboardActivity
import org.openmrs.mobile.databinding.ActivityNewDashboardBinding


@AndroidEntryPoint
class SyncedPatientsActivity : ACBaseActivity(), View.OnClickListener {
    private var query: String? = null
    private var addPatientMenuItem: MenuItem? = null
    private lateinit var mBinding: ActivityNewDashboardBinding
    private val mViewModel: SyncedPatientsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_dashboard)
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = this

        supportActionBar?.let {
            it.elevation = 0f
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.action_synced_patients)
            it.setHomeAsUpIndicator(R.drawable.ic_drawer)
        }
        // Create fragment
        var syncedPatientsFragment = supportFragmentManager.findFragmentById(R.id.syncedPatientsContentFrame) as SyncedPatientsFragment?
        if (syncedPatientsFragment == null) {
            syncedPatientsFragment = SyncedPatientsFragment.newInstance()
        }
        if (!syncedPatientsFragment.isActive) {
            addFragmentToActivity(supportFragmentManager,
                    syncedPatientsFragment, R.id.syncedPatientsContentFrame)
        }

        mViewModel.loadDrawerItems(this)
        mBinding.drawerItemList.adapter = mViewModel.drawerItemListAdapter

        mBinding.drawerView.buttonLogout.setOnClickListener(this)
        observeData()
    }

    private fun observeData() {

        mViewModel.loadFindPatient.observe(this, Observer { gotoPatientList() })

        mViewModel.loadAddPatient.observe(this, Observer { gotoRegisterPatient() })

        mViewModel.loadActiveVisits.observe(this, Observer { gotoActiveVisits() })

        mViewModel.loadFormEntry.observe(this, Observer { gotoFormEntry() })

        mViewModel.loadManageProviders.observe(this, Observer { gotoManageProviders() })

        mViewModel.loadMemberList.observe(this, Observer { gotoMemberList() })

        mViewModel.loadAddMember.observe(this, Observer { gotoRegisterPatient() })

        mViewModel.loadReferredMemberList.observe(this, Observer { gotoReferredMemberList() })

        mViewModel.loadMemberProfile.observe(this, Observer { gotoMemberProfile() })

    }

    fun deletePatient(patient: Patient) {
        val syncedPatientsFragment = supportFragmentManager.findFragmentById(R.id.syncedPatientsContentFrame) as SyncedPatientsFragment?
        syncedPatientsFragment?.deletePatient(patient)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.syncbutton -> enableAddPatient(OpenmrsAndroid.getSyncState())
            R.id.actionAddPatients -> {
                val intent = Intent(this, LastViewedPatientsActivity::class.java)
                startActivity(intent)
            }
            android.R.id.home -> {
                openCloseDrawer()
            }
            else -> {
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.find_locally_and_add_patients_menu, menu)

        addPatientMenuItem = menu.findItem(R.id.actionAddPatients)
        enableAddPatient(OpenmrsAndroid.getSyncState())

        val searchMenuItem = menu.findItem(R.id.actionSearchLocal)
        val searchView = menu.findItem(R.id.actionSearchLocal).actionView as SearchView
        if (notEmpty(query)) {
            searchMenuItem.expandActionView()
            searchView.setQuery(query, true)
            searchView.clearFocus()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                val syncedPatientsFragment = supportFragmentManager.findFragmentById(R.id.syncedPatientsContentFrame) as SyncedPatientsFragment?
                if(NetworkUtils.isOnline() && query.isNotEmpty()){
                    syncedPatientsFragment?.fetchSyncedPatientsOnRefresh(query)
                } else {
                    syncedPatientsFragment?.fetchSyncedPatients(query)
                }
                return true
            }
        })
        return true
    }

    private fun enableAddPatient(enabled: Boolean) {
        val resId = if (enabled) R.drawable.ic_add else R.drawable.ic_add_disabled
        addPatientMenuItem?.let {
            it.isEnabled = enabled
            it.setIcon(resId)
        }
    }

    private fun openCloseDrawer() {
        val dl = mBinding.drawerLayout
        if(dl.isDrawerOpen(GravityCompat.START)){
            dl.closeDrawer(GravityCompat.START);
        }
        else {
            dl.openDrawer(GravityCompat.START);
        }
    }

    fun gotoPatientList() {
        openCloseDrawer()
        val intent = Intent(this, SyncedPatientsActivity::class.java)
        startActivity(intent)
    }

    fun gotoRegisterPatient() {
        openCloseDrawer()
        val intent = Intent(this, AddEditPatientActivity::class.java)
        startActivity(intent)
    }

    fun gotoActiveVisits() {
        openCloseDrawer()
        val intent = Intent(this, ActiveVisitsActivity::class.java)
        startActivity(intent)
    }

    fun gotoFormEntry() {
        openCloseDrawer()
        val intent = Intent(this, FormEntryPatientListActivity::class.java)
        startActivity(intent)
    }

    fun gotoManageProviders() {
        openCloseDrawer()
        val intent = Intent(this, ProviderManagerDashboardActivity::class.java)
        startActivity(intent)
    }

    fun gotoMemberList() {
        openCloseDrawer()
        val intent = Intent(this, MemberListActivity::class.java)
        startActivity(intent)
    }

    fun gotoReferredMemberList() {
        openCloseDrawer()
        val intent = Intent(this, MemberListActivity::class.java)
        startActivity(intent)
    }

    fun gotoMemberProfile() {
        openCloseDrawer()
//        val intent = Intent(this, MemberProfileActivity::class.java)
//        startActivity(intent)
    }

    override fun onRestart() {
        super.onRestart()
        /*finish()
        startActivity(intent)*/
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            mBinding.drawerView.buttonLogout.id -> {}
        }
    }
}
