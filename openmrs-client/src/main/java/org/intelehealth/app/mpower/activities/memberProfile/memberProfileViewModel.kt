package org.intelehealth.app.mpower.activities.memberProfile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.openmrs.android_sdk.library.api.repository.ConceptRepository
import com.openmrs.android_sdk.library.api.repository.EncounterRepository
import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.dao.EncounterDAO
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.models.*
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.DateUtils
import com.openmrs.android_sdk.utilities.execute
import dagger.hilt.android.lifecycle.HiltViewModel
import org.intelehealth.app.mpower.activities.BaseViewModel
import org.intelehealth.app.mpower.activities.formdisplay.FormDisplayActivity
import org.intelehealth.app.mpower.databinding.ActivityMemberProfileBinding
import org.intelehealth.app.mpower.listeners.ItemClickListener
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject


@HiltViewModel
class MemberProfileViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val encounterDAO: EncounterDAO,
    private val patientRepository: PatientRepository,
    private val encounterRepository: EncounterRepository,
    private val conceptRepository: ConceptRepository,
    private val savedStateHandle: SavedStateHandle
    ) : BaseViewModel<Patient>(), ItemClickListener {

    private lateinit var mBinding: ActivityMemberProfileBinding

    var patient: Patient = Patient()
    var firstName: String = ""
    var lastName: String = ""
    var dob: String = ""

    private val _rxReligion = MutableLiveData<String>()
    val rxReligion: LiveData<String> get() = _rxReligion
    private val _rxBloodGroup = MutableLiveData<String>()
    val rxBloodGroup: LiveData<String> get() = _rxBloodGroup
    private val _rxMaritalStatus = MutableLiveData<String>()
    val rxMaritalStatus: LiveData<String> get() = _rxMaritalStatus

    private val _rxEncounterList = MutableLiveData<List<Encounter>>()
    val rxEncounterList: LiveData<List<Encounter>> get() = _rxEncounterList

    var mobile: String = ""
    var nid: String = ""
    var age: String = ""

    private val _rxFormList = MutableLiveData<List<String>>()
    val rxFormList: LiveData<List<String>> get() = _rxFormList

    fun populateProfileData() {
        firstName = patient.person.names[0].givenName ?: ""
        lastName = patient.person.names[0].familyName ?: ""
        age = patient.person.age.toString()
        dob = DateUtils.convertTime1(patient.person.birthdate, DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
        patient.person.attributes.forEach {
            if(it.attributeType?.display.equals(ApplicationConstants.MemberProfileKeys.MEMBER_PROFILE_NID)){
                nid = it.value ?: ""
            } else if(it.attributeType?.display.equals(ApplicationConstants.MemberProfileKeys.MEMBER_PROFILE_MOBILE)){
                mobile = it.value ?: ""
            } else if(it.attributeType?.display.equals(ApplicationConstants.MemberProfileKeys.MEMBER_PROFILE_RELIGION)){
                if(it.value != null && !it.value.equals("")){
                    fetchReligion(it.value!!)
                }
            } else if(it.attributeType?.display.equals(ApplicationConstants.MemberProfileKeys.MEMBER_PROFILE_MARITAL_STATUS)){
                if(it.value != null && !it.value.equals("")){
                    fetchMaritalStatus(it.value!!)
                }
            } else if(it.attributeType?.display.equals(ApplicationConstants.MemberProfileKeys.MEMBER_PROFILE_BLOOD_GROUP)){
                if(it.value != null && !it.value.equals("")){
                    fetchBloodGroup(it.value!!)
                }
            }
        }
    }

    fun populateServiceForm() {
        if (rxMaritalStatus.value!! == ApplicationConstants.AttributeValues.MARRIED && age.toInt() > 15 && patient.person.gender == ApplicationConstants.GENDER.FEMALE){
            val mList = encounterRepository.getLocalEncounterCreateByPatientUUID(patient.uuid!!).execute()
            if(mList != null && mList.isNotEmpty()){
                var breakLoop = false
                val eList = mList.reversed()
                eList.forEach { ec ->
                    if (breakLoop) return@forEach
                    ec.observations.forEach { ob ->
                        if(ob.concept == ApplicationConstants.FormTypeKeys.FORM_PREGNANCY_UUID){
                            when (ob.value) {
                                ApplicationConstants.FormTypeKeys.FORM_PREGNANCY_ANC_UUID -> {
                                    _rxFormList.value = mutableListOf(
                                        ApplicationConstants.FormListKeys.PREGNANCY_SERVICE,
                                        ApplicationConstants.FormListKeys.PRE_PREGNANCY_SERVICE,
                                        ApplicationConstants.FormListKeys.GENERAL_PATIENT_SERVICE
                                    )
                                }
                                ApplicationConstants.FormTypeKeys.FORM_PREGNANCY_PNC_UUID -> {
                                    _rxFormList.value = mutableListOf(
                                        ApplicationConstants.FormListKeys.PREGNANCY_SERVICE,
                                        ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE,
                                        ApplicationConstants.FormListKeys.POST_PREGNANCY_SERVICE,
                                        ApplicationConstants.FormListKeys.GENERAL_PATIENT_SERVICE
                                    )
                                }
                                else -> {
                                    _rxFormList.value = mutableListOf(
                                        ApplicationConstants.FormListKeys.PREGNANCY_SERVICE,
                                        ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE,
                                        ApplicationConstants.FormListKeys.GENERAL_PATIENT_SERVICE
                                    )
                                }
                            }
                            breakLoop = true
                            return@forEach
                        }
                    }
                }
            } else {
                _rxFormList.value = mutableListOf(
                    ApplicationConstants.FormListKeys.PREGNANCY_SERVICE,
                    ApplicationConstants.FormListKeys.PRE_PREGNANCY_SERVICE,
                    ApplicationConstants.FormListKeys.POST_PREGNANCY_SERVICE,
                    ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE,
                    ApplicationConstants.FormListKeys.GENERAL_PATIENT_SERVICE
                )
            }
        }
    }

    fun processFormViews() {
        _rxFormList.value = mutableListOf()
        populateServiceForm()

        _rxEncounterList.value = mutableListOf()
        populateServiceHistory()
    }

    private fun fetchReligion(value: String) {
        addSubscription(conceptRepository.getConceptByUuid(value)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { ca: ConceptAnswers -> _rxReligion.value = ca.display ?: "" },
                { setError(it, OperationType.ConceptSearching) }
            )
        )
    }

    private fun fetchMaritalStatus(value: String) {
        addSubscription(conceptRepository.getConceptByUuid(value)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { ca: ConceptAnswers -> _rxMaritalStatus.value = ca.display ?: "" },
                { setError(it, OperationType.ConceptSearching) }
            )
        )
    }

    private fun fetchBloodGroup(value: String) {
        addSubscription(conceptRepository.getConceptByUuid(value)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { ca: ConceptAnswers -> _rxBloodGroup.value = ca.display ?: "" },
                { setError(it, OperationType.ConceptSearching) }
            )
        )
    }

    fun fetchEncounterType() = encounterDAO.getEncounterTypeByFormName(EncounterType.ADMISSION).uuid!!

    fun populateServiceHistory() {
        addSubscription(encounterRepository.getAllEncounterResourcesByPatientUuid(patient.uuid!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { encounterList: List<Encounter> -> _rxEncounterList.value = encounterList },
                { setError(it, OperationType.EncounterListSearching) }
            )
        )
    }



    fun fetchMembers() {
        setLoading()
    }

    fun fetchMembers(query: String) {
        setLoading()
    }

    override fun onItemClicked(item: Any?) {

    }

}