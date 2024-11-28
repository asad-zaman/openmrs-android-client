package org.intelehealth.app.mpower.syncModule

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.models.CustomPerson
import com.openmrs.android_sdk.library.models.OperationType
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.PatientCreateDTO
import com.openmrs.android_sdk.library.models.Person
import com.openmrs.android_sdk.library.models.PersonAddress
import com.openmrs.android_sdk.library.models.PersonAttribute
import com.openmrs.android_sdk.library.models.PersonAttributeCustom
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.ToastUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.UUID
import javax.inject.Inject


@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val patientRepository: PatientRepository) : Worker(context, workerParams) {

    override fun doWork(): Result {
//        callApi()
        return Result.success()
    }

    /*private fun callApi() {
        val pList = patientRepository.fetchUnSyncedPatients().toBlocking().first()
        for (patient in pList){
            val cp = patient.person.toCustomPerson().apply {
                this.attributes = convertToCustomAttributes(patient.person.attributes)
                if(this.addresses.isEmpty()){
                    this.addresses.add(PersonAddress())
                }
                this.uuid = UUID.randomUUID().toString()
            }
            val pcm = patient.toPatientCreateModel().apply {
                this.person = cp
                this.uuid = cp.uuid
            }
            patient.display = listOfNotNull(patient.person.names[0].givenName, patient.person.names[0].middleName, patient.person.names[0].familyName).joinToString(" ")
            patient.uuid = null
            patientRepository.postToServer(patient, pcm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { registeredPatient -> ToastUtil.success("")
                    },
                    { throwable ->
                    }
                )
        }
    }*/

    private fun convertToCustomAttributes(attributes: List<PersonAttribute>): MutableList<PersonAttributeCustom> {
        return attributes.map { attribute ->
            PersonAttributeCustom().apply {
                attributeType = attribute.uuid
                value = attribute.value
            }
        }.toMutableList()
    }

    private fun Patient.toPatientCreateModel(): PatientCreateDTO {
        return PatientCreateDTO().apply {
            uuid = this@toPatientCreateModel.uuid
        }
    }

    /*private fun setPatientAttributes(patient: Patient) : MutableList<PersonAttributeCustom>{
        val attrValues : MutableList<PersonAttributeCustom> = mutableListOf()


        if(patient.person.attributes){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_NID_UUID
            pa.value = binding.etSelectedIdentifierValue.text.toString()
            attrValues.add(pa)
        }
        if(binding.etBirthRegNumber.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_BRID_UUID
            pa.value = binding.etBirthRegNumber.text.toString()
            attrValues.add(pa)
        }
        if(binding.etMobileNo.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_MOBILE_UUID
            pa.value = binding.etMobileNo.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFullNameBangla.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_FULL_NAME_BANGLA_UUID
            pa.value = binding.etFullNameBangla.text.toString()
            attrValues.add(pa)
        }
        if(binding.etMotherName.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_MOTHER_NAME_UUID
            pa.value = binding.etMotherName.text.toString()
            attrValues.add(pa)
        }
        if(binding.etMotherNameBangla.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_MOTHER_NAME_BANGLA_UUID
            pa.value = binding.etMotherNameBangla.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFatherName.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_FATHER_NAME_UUID
            pa.value = binding.etFatherName.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFatherName.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_FATHER_NAME_UUID
            pa.value = binding.etFatherName.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFatherNameBangla.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_FATHER_NAME_BANGLA_UUID
            pa.value = binding.etFatherNameBangla.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFatherName.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_FATHER_NAME_UUID
            pa.value = binding.etFatherName.text.toString()
            attrValues.add(pa)
        }
        if(binding.etBirthPlace.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_BIRTH_PLACE_UUID
            pa.value = binding.etBirthPlace.text.toString()
            attrValues.add(pa)
        }
        if(binding.etNationality.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_NATIONALITY_UUID
            pa.value = binding.etNationality.text.toString()
            attrValues.add(pa)
        }
        if(binding.etOccupation.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_OCCUPATION_UUID
            pa.value = binding.etOccupation.text.toString()
            attrValues.add(pa)
        }
        if(binding.etEducation.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_EDUCATION_UUID
            pa.value = binding.etEducation.text.toString()
            attrValues.add(pa)
        }
        if(binding.etDisabilityType.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_DISABILITY_TYPE_UUID
            pa.value = binding.etDisabilityType.text.toString()
            attrValues.add(pa)
        }
        if(binding.etEthnicity.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_ETHNICITY_UUID
            pa.value = binding.etEthnicity.text.toString()
            attrValues.add(pa)
        }
        if(binding.addressOne.text.toString().isNotEmpty()){
            val pa = PersonAttributeCustom()
            pa.attributeType = ApplicationConstants.PATIENTS_ADDRESS_UUID
            pa.value = binding.addressOne.text.toString()
            attrValues.add(pa)
        }
        if(viewModel.rxSelectedDivision.value?.locationId != null){
            for (ld in viewModel.divisionList.value!!){
                if(ld.locationId == viewModel.rxSelectedDivision.value?.locationId){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_DIVISION_UUID
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttributeCustom()
                    pa2.attributeType = ApplicationConstants.PATIENTS_DIVISION_ID_UUID
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedDistrict.value?.locationId != null){
            for (ld in viewModel.districtList.value!!){
                if(ld.locationId == viewModel.rxSelectedDistrict.value?.locationId){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_DISTRICT_UUID
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttributeCustom()
                    pa2.attributeType = ApplicationConstants.PATIENTS_DISTRICT_ID_UUID
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedUpazila.value?.locationId != null){
            for (ld in viewModel.upazilaList.value!!){
                if(ld.locationId == viewModel.rxSelectedUpazila.value?.locationId){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_UPAZILA_UUID
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttributeCustom()
                    pa2.attributeType = ApplicationConstants.PATIENTS_UPAZILA_ID_UUID
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedPaurasava.value?.locationId != null){
            for (ld in viewModel.paurasavaList.value!!){
                if(ld.locationId == viewModel.rxSelectedPaurasava.value?.locationId){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_PAURASAVA_UUID
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttributeCustom()
                    pa2.attributeType = ApplicationConstants.PATIENTS_PAURASAVA_ID_UUID
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedUnion.value?.locationId != null){
            for (ld in viewModel.unionList.value!!){
                if(ld.locationId == viewModel.rxSelectedUnion.value?.locationId){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_UNION_UUID
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttributeCustom()
                    pa2.attributeType = ApplicationConstants.PATIENTS_UNION_ID_UUID
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedWard.value?.locationId != null){
            for (ld in viewModel.wardList.value!!){
                if(ld.locationId == viewModel.rxSelectedWard.value?.locationId){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_WARD_UUID
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttributeCustom()
                    pa2.attributeType = ApplicationConstants.PATIENTS_WARD_ID_UUID
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.selectedMaritalStatusOption.uuid != null){
            for (ld in viewModel.mStatusOptionList.value!!){
                if(ld.uuid == viewModel.selectedMaritalStatusOption.uuid){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_MARITAL_STATUS_UUID
                    pa.value = ld.uuid
                    attrValues.add(pa)
                }
            }
        }
        if(viewModel.selectedBloodGroupOption.uuid != null){
            for (ld in viewModel.bloodGroupOptionList.value!!){
                if(ld.uuid == viewModel.selectedBloodGroupOption.uuid){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_BLOOD_GROUP_UUID
                    pa.value = ld.uuid
                    attrValues.add(pa)
                }
            }
        }
        if(viewModel.selectedReligionOption.uuid != null){
            for (ld in viewModel.religionOptionList.value!!){
                if(ld.uuid == viewModel.selectedReligionOption.uuid){
                    val pa = PersonAttributeCustom()
                    pa.attributeType = ApplicationConstants.PATIENTS_RELIGION_UUID
                    pa.value = ld.uuid
                    attrValues.add(pa)
                }
            }
        }

        return attrValues
    }*/

    private fun Person.toCustomPerson(): CustomPerson {
        return CustomPerson().apply {
            names = this@toCustomPerson.names
            gender = this@toCustomPerson.gender
            uuid = this@toCustomPerson.uuid
            birthdate = this@toCustomPerson.birthdate
            addresses = this@toCustomPerson.addresses
            birthdateEstimated = this@toCustomPerson.birthdateEstimated
        }
    }
}
