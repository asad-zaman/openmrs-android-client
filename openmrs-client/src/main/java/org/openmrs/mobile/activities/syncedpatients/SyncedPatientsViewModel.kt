package org.openmrs.mobile.activities.syncedpatients

import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.dao.VisitDAO
import com.openmrs.android_sdk.library.models.OperationType
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.utilities.NetworkUtils
import com.openmrs.android_sdk.utilities.ToastUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import org.openmrs.mobile.activities.BaseViewModel
import org.openmrs.mobile.utilities.FilterUtil
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class SyncedPatientsViewModel @Inject constructor(private val patientDAO: PatientDAO, private val visitDAO: VisitDAO, private val patientRepository: PatientRepository) : BaseViewModel<List<Patient>>() {

    fun fetchSyncedPatients() {
        setLoading()
        addSubscription(patientDAO.allPatients
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { patients: List<Patient> -> setContent(patients) },
                        { setError(it, OperationType.PatientFetching) }
                ))
    }

    fun fetchSyncedPatients(query: String) {
        setLoading()
        addSubscription(patientDAO.allPatients
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { patients: List<Patient> ->
                            val filteredPatients = FilterUtil.getPatientsFilteredByQuery(patients, query)
                            setContent(filteredPatients)
                        },
                        { setError(it, OperationType.PatientSearching) }
                ))
    }

    fun fetchSyncedPatientsOnRefresh(query: String) {
        if (NetworkUtils.isOnline()) {
            setLoading()
            addSubscription(patientRepository.findPatients(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { patients: List<Patient> ->
                        insertServerPatients(patients)
                        setContent(patients)
                    },
                    { setError(it, OperationType.PatientFetching) }
                )
            )
        }
    }

    fun insertServerPatients(patients: List<Patient>) {
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

    fun deleteSyncedPatient(patient: Patient) {
        setLoading()
        patientDAO.deletePatient(patient.id!!)
        addSubscription(visitDAO.deleteVisitsByPatientId(patient.id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe())
    }
}
