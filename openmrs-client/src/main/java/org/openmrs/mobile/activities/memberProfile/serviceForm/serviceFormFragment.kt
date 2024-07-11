package org.openmrs.mobile.activities.memberProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.activities.BaseFragment
import com.openmrs.android_sdk.library.models.Result
import com.openmrs.android_sdk.utilities.ApplicationConstants
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.patientdashboard.details.PatientDetailsFragment
import org.openmrs.mobile.databinding.FragmentServiceFormBinding

@AndroidEntryPoint
class ServiceFormFragment : BaseFragment() {
    private var _binding: FragmentServiceFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemberProfileViewModel by viewModels()

    companion object {
        fun newInstance(patientId: String): ServiceFormFragment {
            val fragment = ServiceFormFragment()
            fragment.arguments = bundleOf(Pair(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientId))
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentServiceFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(this.activity)
        with(binding) {
            setupObserver()
            fetchMembers()
        }
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> showLoading()
                is Result.Success -> {}
                else -> showError()
            }
        })
    }


    fun fetchMembers() {
        viewModel.fetchMembers()
    }

    fun fetchMembersOnRefresh(query: String) {
        viewModel.fetchMembers(query)
    }

    fun fetchMembers(query: String) {
        viewModel.fetchMembers(query)
    }

    private fun showLoading() {
        with(binding) {

        }
    }

    private fun showError() {
        with(binding) {

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