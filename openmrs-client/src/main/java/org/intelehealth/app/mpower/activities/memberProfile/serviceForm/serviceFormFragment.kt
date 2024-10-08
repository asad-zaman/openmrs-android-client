package org.intelehealth.app.mpower.activities.memberProfile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.Result
import com.openmrs.android_sdk.utilities.ApplicationConstants
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.mpower.activities.BaseFragment
import org.intelehealth.app.mpower.activities.formdisplay.FormDisplayActivity
import org.intelehealth.app.mpower.databinding.FragmentServiceFormBinding

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
            if(mViewModel.rxFormList.value!!.isNotEmpty()) {
                generateFormChips()
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
                    val mEncounterType = mViewModel.fetchEncounterType()
                    Intent(context, FormDisplayActivity::class.java).apply {
                        putExtra(ApplicationConstants.BundleKeys.FORM_NAME, formName)
                        putExtra(ApplicationConstants.BundleKeys.PATIENT_ENTITY, Gson().toJson(patient))
                        putExtra(ApplicationConstants.BundleKeys.VALUEREFERENCE, "")
                        putExtra(ApplicationConstants.BundleKeys.ENCOUNTERTYPE, mEncounterType)
                        startActivityForResult(this, ApplicationConstants.RequestCodes.FORM_DISPLAY_LOCAL_SUCCESS_CODE)
                    }
                }
            }
            binding.formChipGroup.addView(chip)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ApplicationConstants.RequestCodes.FORM_DISPLAY_LOCAL_SUCCESS_CODE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra(ApplicationConstants.ResultKeys.FORM_DISPLAY_LOCAL_SUCCESS_RESULT_KEY)
            if(result == "Success"){
                binding.formChipGroup.removeAllViews()
                mViewModel.processFormViews()
            }
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