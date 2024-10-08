package org.intelehealth.app.mpower.activities.referedMemberList

import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.models.OperationType
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.ReferredPatient
import com.openmrs.android_sdk.utilities.NetworkUtils
import com.openmrs.android_sdk.utilities.ToastUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import org.intelehealth.app.mpower.activities.BaseViewModel
import org.intelehealth.app.mpower.listeners.ItemClickListener
import org.intelehealth.app.mpower.utilities.FilterUtil
import rx.android.schedulers.AndroidSchedulers

import javax.inject.Inject


@HiltViewModel
class ReferedMembersViewModel @Inject constructor(private val patientDAO: PatientDAO, private val patientRepository: PatientRepository) : BaseViewModel<List<Patient>>(),
    ItemClickListener {

    fun fetchMembers() {
        setLoading()
        addSubscription(patientDAO.allPatients
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { patients: List<Patient> -> setContent(patients) },
                { setError(it, OperationType.MemberFetching) }
            ))
    }

    fun fetchMembers(query: String) {
        setLoading()
        addSubscription(patientDAO.allPatients
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { patients: List<Patient> ->
                    val filteredPatients = FilterUtil.getPatientsFilteredByQuery(patients, query)
                    setContent(filteredPatients)
                },
                { setError(it, OperationType.MemberSearching) }
            ))
    }

    fun fetchReferredMembersOnRefresh(query: String) {
        if (NetworkUtils.isOnline()) {
            setLoading()
            addSubscription(patientRepository.findReferredPatients(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { patients: List<ReferredPatient> ->
                        print("done")
//                        insertServerMembers(patients)
//                        setContent(patients)
                    },
                    { setError(it, OperationType.MemberFetching) }
                )
            )
        }
    }

    fun insertServerMembers(patients: List<Patient>) {
        for (patient in patients) {
            if(patient.uuid != null){
                val isSaved = patientDAO.isUserAlreadySaved(patient.uuid!!)
                if(!isSaved){
                    try {
                        addSubscription(patientDAO.savePatient(patient)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe())
                    } catch (e: Error){
                        ToastUtil.error(e.toString())
                    }
                }
            }
        }
    }

    override fun onItemClicked(item: Any?) {

    }

}