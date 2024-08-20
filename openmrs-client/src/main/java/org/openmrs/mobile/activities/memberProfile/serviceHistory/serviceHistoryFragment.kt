package org.openmrs.mobile.activities.memberProfile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.openmrs.android_sdk.library.models.Encounter
import com.openmrs.android_sdk.library.models.Patient
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.activities.BaseFragment
import com.openmrs.android_sdk.library.models.Result
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.ToastUtil
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsRecyclerViewAdapter
import org.openmrs.mobile.databinding.FragmentServiceHistoryBinding
import org.openmrs.mobile.utilities.makeGone
import org.openmrs.mobile.utilities.makeVisible

@AndroidEntryPoint
class ServiceHistoryFragment : BaseFragment() {
    private var _binding: FragmentServiceHistoryBinding? = null
    private val mBinding get() = _binding!!

    private val mViewModel: MemberProfileViewModel by activityViewModels()
    private var patient: Patient = Patient()

    companion object {
        fun newInstance(mPatient: Patient): ServiceHistoryFragment {
            val fragment = ServiceHistoryFragment()
            fragment.arguments = Bundle().also {
                it.putString(ApplicationConstants.BundleKeys.PATIENT_ENTITY, Gson().toJson(mPatient))
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            patient = Gson().fromJson(it.getString(ApplicationConstants.BundleKeys.PATIENT_ENTITY), Patient::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentServiceHistoryBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(this.activity)

        with(mBinding) {
            rvServiceHistoryList.setHasFixedSize(true)
            rvServiceHistoryList.layoutManager = linearLayoutManager
            rvServiceHistoryList.adapter = ServiceHistoryRecyclerAdapter(this@ServiceHistoryFragment, ArrayList())
        }

        setupObserver()
        fetchMembers()
    }

    private fun setupObserver() {
        mViewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> showLoading()
                is Result.Success -> {}
                else -> showError()
            }
        })
        mViewModel.rxEncounterList.observe(viewLifecycleOwner, Observer {
            if(mViewModel.rxEncounterList.value!!.isNotEmpty()) {
                showServiceHistoryList(mViewModel.rxEncounterList.value!!)
            }
        })
    }

    private fun showServiceHistoryList(serviceHistoryList: List<Encounter>) {
        with(mBinding) {
            if (serviceHistoryList.isEmpty()) {
                rvServiceHistoryList.makeGone()
                showEmptyListText()
            } else {
                (rvServiceHistoryList.adapter as ServiceHistoryRecyclerAdapter).updateList(serviceHistoryList)
                rvServiceHistoryList.makeVisible()
                hideEmptyListText()
            }
        }
    }

    fun fetchMembers() {
        mViewModel.fetchMembers()
    }

    private fun showLoading() {
        with(mBinding) {

        }
    }

    private fun showError() {
        with(mBinding) {

        }
        showEmptyListText()
    }

    private fun showEmptyListText() {

    }

    private fun hideEmptyListText() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}