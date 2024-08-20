package org.openmrs.mobile.activities.memberProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.openmrs.android_sdk.library.models.EncounterType
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.Result
import com.openmrs.android_sdk.utilities.ApplicationConstants
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.activities.BaseFragment
import org.openmrs.mobile.activities.formdisplay.FormDisplayActivity
import org.openmrs.mobile.databinding.FragmentServiceFormBinding

@AndroidEntryPoint
class ServiceFormFragment : BaseFragment() {
    private var _binding: FragmentServiceFormBinding? = null
    private val binding get() = _binding!!

    private val mViewModel: MemberProfileViewModel by activityViewModels()
    private var patient: Patient = Patient()

    companion object {
        fun newInstance(mPatient: Patient): ServiceFormFragment {
            val fragment = ServiceFormFragment()
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
        _binding = FragmentServiceFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        fetchMembers()
    }

    private fun setupObserver() {
        mViewModel.result.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Loading -> showLoading()
                is Result.Success -> {}
                else -> showError()
            }
        })
        mViewModel.rxMaritalStatus.observe(viewLifecycleOwner, Observer {
            mViewModel.populateServiceForm()
        })
        mViewModel.rxFormList.observe(viewLifecycleOwner, Observer {
            if(mViewModel.rxFormList.value!!.isNotEmpty() && !mViewModel.chipStateUpdated) {
                generateFormChips()
                mViewModel.chipStateUpdated = true
            }
        })
    }

    private fun generateFormChips() {
        for (formName in mViewModel.rxFormList.value!!) {
            val chip = Chip(context).apply {
                text = formName
                isCloseIconVisible = false
                setOnCloseIconClickListener { selectedChip: View? ->
                    binding.formChipGroup.removeView(selectedChip)
                }
                setOnClickListener {
                    mViewModel.gotoFormDisplay(formName, context)
                }
            }
            binding.formChipGroup.addView(chip)
        }
    }

    private fun fetchMembers() {
        mViewModel.fetchMembers()
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