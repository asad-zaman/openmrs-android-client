package org.openmrs.mobile.activities.dynamicForm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
//import com.google.android.libraries.places.api.net.PlacesClient
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
class DynamicFormViewModel @Inject constructor(
        private val patientDAO: PatientDAO,
        private val locationRepository: LocationRepository,
        private val patientRepository: PatientRepository,
        private val conceptRepository: ConceptRepository,
        private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Patient>() {

    private val _similarPatientsLiveData = MutableLiveData<List<Patient>>()
    val similarPatientsLiveData: LiveData<List<Patient>> get() = _similarPatientsLiveData

    init {

    }
}
