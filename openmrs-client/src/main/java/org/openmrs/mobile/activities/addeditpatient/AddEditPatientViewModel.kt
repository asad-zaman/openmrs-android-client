package org.openmrs.mobile.activities.addeditpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.android.libraries.places.api.net.PlacesClient
import com.openmrs.android_sdk.library.api.repository.ConceptRepository
import com.openmrs.android_sdk.library.api.repository.LocationRepository
import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.models.*
import com.openmrs.android_sdk.library.models.OperationType.FetchingSearchUser
import com.openmrs.android_sdk.library.models.OperationType.PatientRegistering
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.COUNTRIES_BUNDLE
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import com.openmrs.android_sdk.utilities.PatientValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import org.joda.time.DateTime
import org.openmrs.mobile.activities.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AddEditPatientViewModel @Inject constructor(
        private val patientDAO: PatientDAO,
        private val locationRepository: LocationRepository,
        private val patientRepository: PatientRepository,
        private val conceptRepository: ConceptRepository,
        private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Patient>() {

    private val _similarPatientsLiveData = MutableLiveData<List<Patient>>()
    val similarPatientsLiveData: LiveData<List<Patient>> get() = _similarPatientsLiveData

    private val _patientUpdateLiveData = MutableLiveData<ResultType>()
    val patientUpdateLiveData: LiveData<ResultType> get() = _patientUpdateLiveData

    private val _divisionList = MutableLiveData<List<LocationData>>()
    val divisionList: LiveData<List<LocationData>> get() = _divisionList

    private val _districtList = MutableLiveData<List<LocationData>>()
    val districtList: LiveData<List<LocationData>> get() = _districtList

    private val _upazilaList = MutableLiveData<List<LocationData>>()
    val upazilaList: LiveData<List<LocationData>> get() = _upazilaList

    private val _paurasavaList = MutableLiveData<List<LocationData>>()
    val paurasavaList: LiveData<List<LocationData>> get() = _paurasavaList

    private val _unionList = MutableLiveData<List<LocationData>>()
    val unionList: LiveData<List<LocationData>> get() = _unionList

    private val _wardList = MutableLiveData<List<LocationData>>()
    val wardList: LiveData<List<LocationData>> get() = _wardList

    private val _blockList = MutableLiveData<List<LocationData>>()
    val blockList: LiveData<List<LocationData>> get() = _blockList

    private val _mStatusOptionList = MutableLiveData<List<ConceptOption>>()
    val mStatusOptionList: LiveData<List<ConceptOption>> get() = _mStatusOptionList

    var selectedMaritalStatusOption : ConceptOption = ConceptOption()

    private val _bloodGroupOptionList = MutableLiveData<List<ConceptOption>>()
    val bloodGroupOptionList: LiveData<List<ConceptOption>> get() = _bloodGroupOptionList

    var selectedBloodGroupOption : ConceptOption = ConceptOption()

    private val _religionOptionList = MutableLiveData<List<ConceptOption>>()
    val religionOptionList: LiveData<List<ConceptOption>> get() = _religionOptionList

    var selectedReligionOption : ConceptOption = ConceptOption()


    val genderList: List<String> = arrayListOf("Male", "Female", "Other", "Unknown")
    var selectedGender = ""

    val mIdentifierList: List<String> = arrayListOf("NID", "HID", "BRID")
    var selectedIdentifier = ""

    val mIdentifierTypeList: List<String> = arrayListOf("NID", "BRN", "কোনটা না")
    var selectedIdentifierType = ""

    var rxSelectedDivision: MutableLiveData<LocationData> = MutableLiveData()
    var rxSelectedDistrict: MutableLiveData<LocationData> = MutableLiveData()
    var rxSelectedUpazila: MutableLiveData<LocationData> = MutableLiveData()
    var rxSelectedPaurasava: MutableLiveData<LocationData> = MutableLiveData()
    var rxSelectedUnion: MutableLiveData<LocationData> = MutableLiveData()
    var rxSelectedWard: MutableLiveData<LocationData> = MutableLiveData()
    var rxSelectedBlock: MutableLiveData<LocationData> = MutableLiveData()

    var patientValidator: PatientValidator

    private val _mSearchUser = MutableLiveData<SearchUser>()
    val mSearchUser: LiveData<SearchUser> get() = _mSearchUser

    var isUpdatePatient = false
        private set

    lateinit var patient: Patient
        private set
    var isPatientUnidentified = false
        set(value) {
            field = value
            patientValidator.isPatientUnidentified = value
        }

    var placesClient: PlacesClient? = null
    var dateHolder: DateTime? = null
    var identifierDateHolder: DateTime? = null
    var capturedPhotoFile: File? = null

    init {
        // Initialize patient state
        val patientId: String? = savedStateHandle.get(PATIENT_ID_BUNDLE)
        val foundPatient = patientDAO.findPatientByID(patientId)
        if (foundPatient != null) {
            isUpdatePatient = true
            patient = foundPatient
        } else {
            resetPatient()
        }

        // Get available countries picker list
        val countriesList: List<String> = savedStateHandle.get(COUNTRIES_BUNDLE)!!

        // Initialize patient data validator
        patientValidator = PatientValidator(patient, isPatientUnidentified, countriesList)
    }

    fun resetPatient() {
        isUpdatePatient = false
        capturedPhotoFile = null
        dateHolder = null
        patient = Patient()
    }

    fun confirmPatient() {
        val aa = patientValidator.validate()
        if (!patientValidator.validate()) return
        /*if (isUpdatePatient) updatePatient()
        else registerPatient()*/
    }

    fun fetchSimilarPatients() {
        if (!patientValidator.validate()) return
        if (isPatientUnidentified) {
            _similarPatientsLiveData.value = emptyList()
            return
        }
        setLoading()
        addSubscription(patientRepository.fetchSimilarPatients(patient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _similarPatientsLiveData.value = it }
        )
    }

    fun onSearch(searchBody: SearchRequest) {
        setLoading()
        addSubscription(
            locationRepository.getUserBySearchIdentifier(searchBody)
//            patientRepository.getUserBySearchIdentifier(searchBody)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _mSearchUser.value = it },
                { setError(it, FetchingSearchUser) }
            )
        )
    }

    fun onSignUp(patientCreateBody: PatientCreate) {
        setLoading()
        addSubscription(
            locationRepository.postPatientCreate(patientCreateBody)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _mSearchUser.value = it },
                    { setError(it, FetchingSearchUser) }
                )
        )
    }

    fun fetchMaritalStatusOptions() {
        setLoading()
        addSubscription(conceptRepository.getConceptOptions(ApplicationConstants.PATIENTS_MARITAL_STATUS_OPTIONS_UUID)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _mStatusOptionList.value = it.answers
            }
        )
    }

    fun fetchBloodGroupOptions() {
        setLoading()
        addSubscription(conceptRepository.getConceptOptions(ApplicationConstants.PATIENTS_BLOOD_GROUP_OPTIONS_UUID)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _bloodGroupOptionList.value = it.answers
            }
        )
    }

    fun fetchReligionOptions() {
        setLoading()
        addSubscription(conceptRepository.getConceptOptions(ApplicationConstants.PATIENTS_RELIGION_OPTIONS_UUID)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _religionOptionList.value = it.answers
            }
        )
    }

    fun fetchServerDivisions() {
        setLoading()
        addSubscription(locationRepository.getAddresses(ApplicationConstants.COUNTRY_CODE_BD)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _divisionList.value = it
            }
        )
    }

    fun fetchServerDistricts() {
        setLoading()
        addSubscription(locationRepository.getAddresses(rxSelectedDivision.value?.locationId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _districtList.value = it
            }
        )
    }

    fun fetchServerUpazilas() {
        setLoading()
        addSubscription(locationRepository.getAddresses(rxSelectedDistrict.value?.locationId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _upazilaList.value = it
            }
        )
    }

    fun fetchServerPaurasavas() {
        setLoading()
        addSubscription(locationRepository.getAddresses(rxSelectedUpazila.value?.locationId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _paurasavaList.value = it
            }
        )
    }

    fun fetchServerUnions() {
        setLoading()
        addSubscription(locationRepository.getAddresses(rxSelectedPaurasava.value?.locationId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _unionList.value = it
            }
        )
    }

    fun fetchServerWards() {
        setLoading()
        addSubscription(locationRepository.getAddresses(rxSelectedUnion.value?.locationId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _wardList.value = it }
        )
    }

    fun fetchServerBlocks() {
        setLoading()
        addSubscription(locationRepository.getAddresses(rxSelectedWard.value?.locationId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _blockList.value = it
            }
        )
    }

    fun fetchCausesOfDeath(): LiveData<ConceptAnswers> {
        val liveData = MutableLiveData<ConceptAnswers>()
        addSubscription(patientRepository.getCauseOfDeathGlobalConceptID()
                .flatMap { conceptRepository.getConceptByUuid(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { causesOfDeath: ConceptAnswers -> liveData.value = causesOfDeath },
                        { throwable -> liveData.value = ConceptAnswers() }
                )
        )
        return liveData
    }

    private fun registerPatient() {
        setLoading()
        addSubscription(patientRepository.registerPatient(patient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { setContent(it, PatientRegistering) },
                        { setError(it, PatientRegistering) }
                )
        )
    }

    private fun updatePatient() {
        setLoading()
        addSubscription(patientRepository.updatePatient(patient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { resultType -> _patientUpdateLiveData.value = resultType },
                        { _patientUpdateLiveData.value = ResultType.PatientUpdateError }
                )
        )
    }
}
