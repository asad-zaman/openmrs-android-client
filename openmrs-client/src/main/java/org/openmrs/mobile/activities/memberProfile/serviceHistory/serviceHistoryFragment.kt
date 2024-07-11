package org.openmrs.mobile.activities.memberProfile

import android.content.Context
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
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardActivity
import org.openmrs.mobile.databinding.FragmentServiceHistoryBinding

@AndroidEntryPoint
class ServiceHistoryFragment : BaseFragment() {
    private var _binding: FragmentServiceHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemberProfileViewModel by viewModels()
    private lateinit var memberProfileActivity: MemberProfileActivity

    companion object {
        fun newInstance(patientId: String): ServiceHistoryFragment {
            val fragment = ServiceHistoryFragment()
            fragment.arguments = bundleOf(Pair(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientId))
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        memberProfileActivity = context as MemberProfileActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentServiceHistoryBinding.inflate(inflater, container, false)
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