package org.openmrs.mobile.activities.memberProfile

import androidx.lifecycle.SavedStateHandle
import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import org.openmrs.mobile.activities.BaseViewModel
import org.openmrs.mobile.listeners.ItemClickListener

import javax.inject.Inject


@HiltViewModel
class MemberProfileViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val patientRepository: PatientRepository,
    private val savedStateHandle: SavedStateHandle
    ) : BaseViewModel<Patient>(), ItemClickListener {

//    val patientId: String = savedStateHandle.get<Long>(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE)?.toString()!!
//    private val patient: Patient = patientDAO.findPatientByID(patientId)
//    private val patientUuid: String = patient.uuid!!

    fun fetchMembers() {
        setLoading()
    }

    fun fetchMembers(query: String) {
        setLoading()
    }

    override fun onItemClicked(item: Any?) {

    }

}