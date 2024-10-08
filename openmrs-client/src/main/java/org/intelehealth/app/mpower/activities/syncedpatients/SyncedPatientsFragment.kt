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
package org.intelehealth.app.mpower.activities.syncedpatients

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.Result
import com.openmrs.android_sdk.utilities.ApplicationConstants
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.activities.BaseFragment
import org.intelehealth.app.mpower.activities.addeditpatient.AddEditPatientActivity
import org.intelehealth.app.mpower.activities.formlist.FormListActivity
import org.intelehealth.app.mpower.databinding.FragmentSyncedPatientsBinding
import org.intelehealth.app.mpower.utilities.makeGone
import org.intelehealth.app.mpower.utilities.makeInvisible
import org.intelehealth.app.mpower.utilities.makeVisible

@AndroidEntryPoint
class SyncedPatientsFragment : BaseFragment(), View.OnClickListener{
    private var _binding: FragmentSyncedPatientsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyncedPatientsViewModel by activityViewModels()

    companion object {
        fun newInstance(): SyncedPatientsFragment {
            return SyncedPatientsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSyncedPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(this.activity)
        with(binding) {
            syncedPatientRecyclerView.setHasFixedSize(true)
            syncedPatientRecyclerView.layoutManager = linearLayoutManager
            syncedPatientRecyclerView.adapter =
                SyncedPatientsRecyclerViewAdapter(
                    this@SyncedPatientsFragment,
                    ArrayList()
                )

            setupListeners()
            setupObserver()
            fetchSyncedPatients()

            swipeLayout.setOnRefreshListener {
                fetchSyncedPatients()
                swipeLayout.isRefreshing = false
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddPatient.setOnClickListener(this@SyncedPatientsFragment)
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> showLoading()
                is Result.Success -> showPatientsList(result.data)
                else -> showError()
            }
        })
    }

    fun fetchSyncedPatients() {
        viewModel.fetchSyncedPatients()
    }

    fun fetchSyncedPatientsOnRefresh(query: String) {
        viewModel.fetchSyncedPatientsOnRefresh(query)
    }

    fun fetchSyncedPatients(query: String) {
        viewModel.fetchSyncedPatients(query)
    }

    fun deletePatient(patient: Patient) {
        viewModel.deleteSyncedPatient(patient)
    }

    private fun showLoading() {
        with(binding) {
            syncedPatientsInitialProgressBar.makeInvisible()
            syncedPatientRecyclerView.makeGone()
        }
    }

    private fun showPatientsList(patients: List<Patient>) {
        with(binding) {
            syncedPatientsInitialProgressBar.makeGone()
            if (patients.isEmpty()) {
                syncedPatientRecyclerView.makeGone()
                showEmptyListText()
            } else {
                (syncedPatientRecyclerView.adapter as SyncedPatientsRecyclerViewAdapter).updateList(patients)
                syncedPatientRecyclerView.makeVisible()
                hideEmptyListText()
            }
        }
    }

    private fun showError() {
        with(binding) {
            syncedPatientsInitialProgressBar.makeGone()
            syncedPatientRecyclerView.makeGone()
        }
        showEmptyListText()
    }

    private fun showEmptyListText() {
        binding.emptySyncedPatientList.makeVisible()
        binding.emptySyncedPatientList.text = getString(R.string.search_patient_no_results)
    }

    private fun hideEmptyListText() {
        binding.emptySyncedPatientList.makeGone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun gotoRegisterPatient() {
        val intent = Intent(activity, AddEditPatientActivity::class.java)
        /*val intent = Intent(activity, FormListActivity::class.java)
        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, "1e9a8f47-e50e-4cd1-a4f6-68d412629671")*/
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.fabAddPatient.id -> gotoRegisterPatient()
        }
    }
}
