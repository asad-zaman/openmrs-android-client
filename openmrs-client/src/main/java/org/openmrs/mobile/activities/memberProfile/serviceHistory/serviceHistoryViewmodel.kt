package org.openmrs.mobile.activities.memberProfile.serviceHistory

import androidx.lifecycle.SavedStateHandle
import com.openmrs.android_sdk.library.api.repository.VisitRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.dao.VisitDAO
import com.openmrs.android_sdk.library.models.Visit
import org.openmrs.mobile.activities.BaseViewModel
import javax.inject.Inject

class ServiceHistoryViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitDAO: VisitDAO,
    private val visitRepository: VisitRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<List<Visit>>(){
}