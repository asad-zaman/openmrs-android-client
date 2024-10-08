package org.intelehealth.app.mpower.activities.syncedpatients

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.dao.VisitDAO
import com.openmrs.android_sdk.library.models.OperationType
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.utilities.NetworkUtils
import com.openmrs.android_sdk.utilities.ToastUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import org.intelehealth.app.mpower.activities.BaseViewModel
import org.intelehealth.app.mpower.listeners.ItemClickListener
import org.intelehealth.app.mpower.models.NavDrawerItem
import org.intelehealth.app.mpower.resources.Constants
import org.intelehealth.app.mpower.utilities.FilterUtil
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class SyncedPatientsViewModel @Inject constructor(private val patientDAO: PatientDAO, private val visitDAO: VisitDAO, private val patientRepository: PatientRepository) : BaseViewModel<List<Patient>>(),
    ItemClickListener {
    var drawerItems = arrayListOf<NavDrawerItem>()
    val drawerItemListAdapter: NavDrawerAdapter = NavDrawerAdapter(this, drawerItems)

    var loadFindPatient : MutableLiveData<Boolean> = MutableLiveData()
    var loadAddPatient : MutableLiveData<Boolean> = MutableLiveData()
    var loadActiveVisits : MutableLiveData<Boolean> = MutableLiveData()
    var loadFormEntry : MutableLiveData<Boolean> = MutableLiveData()
    var loadManageProviders : MutableLiveData<Boolean> = MutableLiveData()
    var loadMemberList : MutableLiveData<Boolean> = MutableLiveData()
    var loadReferredMemberList : MutableLiveData<Boolean> = MutableLiveData()
    var loadAddMember : MutableLiveData<Boolean> = MutableLiveData()

    fun loadDrawerItems(context: Context) {
        drawerItemListAdapter.updateModuleItems(NavDrawerItem.getNavDrawerItems(context))
    }

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

    fun gotoRegisterPatient(view: View) {
        if(loadAddPatient.value == null)
            loadAddPatient.value = true
    }

    override fun onItemClicked(item: Any?) {
        val navDrawer = item as NavDrawerItem
        if(navDrawer.id == Constants.ITEM_FIND_PATIENT) {
            if(loadFindPatient.value == null)
                loadFindPatient.value = true
            else {
                loadFindPatient.value = loadFindPatient.value != true
            }
        } else if(navDrawer.id == Constants.ITEM_ADD_PATIENT) {
            if(loadAddPatient.value == null)
                loadAddPatient.value = true
            else {
                loadAddPatient.value = loadAddPatient.value != true
            }
        } else if(navDrawer.id == Constants.ITEM_ACTIVE_VISITS) {
            if(loadActiveVisits.value == null)
                loadActiveVisits.value = true
            else {
                loadActiveVisits.value = loadActiveVisits.value != true
            }
        } else if(navDrawer.id == Constants.ITEM_FORM_ENTRY) {
            if(loadFormEntry.value == null)
                loadFormEntry.value = true
            else {
                loadFormEntry.value = loadFormEntry.value != true
            }
        } else if(navDrawer.id == Constants.ITEM_MANAGE_PROVIDERS) {
            if(loadManageProviders.value == null)
                loadManageProviders.value = true
            else {
                loadManageProviders.value = loadManageProviders.value != true
            }
        }
        else if(navDrawer.id == Constants.ITEM_FIND_MEMBER) {
            if(loadMemberList.value == null)
                loadMemberList.value = true
            else {
                loadMemberList.value = loadMemberList.value != true
            }
        }
        else if(navDrawer.id == Constants.ITEM_REFERRED_MEMBER_LIST) {
            if(loadReferredMemberList.value == null)
                loadReferredMemberList.value = true
            else {
                loadReferredMemberList.value = loadReferredMemberList.value != true
            }
        }
        else if(navDrawer.id == Constants.ITEM_ADD_MEMBER) {
            if(loadAddMember.value == null)
                loadAddMember.value = true
            else {
                loadAddMember.value = loadAddMember.value != true
            }
        }
    }

}
