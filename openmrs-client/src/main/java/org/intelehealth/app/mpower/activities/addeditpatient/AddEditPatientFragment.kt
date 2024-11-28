/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.intelehealth.app.mpower.activities.addeditpatient

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.annotation.StringDef
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
//import com.google.android.gms.common.api.ApiException
//import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.AutocompleteSessionToken
//import com.google.android.libraries.places.api.model.TypeFilter
//import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
//import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.snackbar.Snackbar
import com.openmrs.android_sdk.library.models.*
import com.openmrs.android_sdk.library.models.OperationType.PatientRegistering
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.COUNTRIES_BUNDLE
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import com.openmrs.android_sdk.utilities.ApplicationConstants.URI_IMAGE
import com.openmrs.android_sdk.utilities.DateUtils
import com.openmrs.android_sdk.utilities.DateUtils.convertTime
import com.openmrs.android_sdk.utilities.DateUtils.convertTimeString
import com.openmrs.android_sdk.utilities.DateUtils.getDateTimeFromDifference
import com.openmrs.android_sdk.utilities.DateUtils.validateDate
import com.openmrs.android_sdk.utilities.NetworkUnavailableException
import com.openmrs.android_sdk.utilities.StringUtils.ILLEGAL_ADDRESS_CHARACTERS
import com.openmrs.android_sdk.utilities.StringUtils.ILLEGAL_CHARACTERS
import com.openmrs.android_sdk.utilities.StringUtils.isBlank
import com.openmrs.android_sdk.utilities.StringUtils.notEmpty
import com.openmrs.android_sdk.utilities.StringUtils.notNull
import com.openmrs.android_sdk.utilities.StringUtils.validateText
import com.openmrs.android_sdk.utilities.ToastUtil
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCrop.REQUEST_CROP
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.activities.BaseFragment
import org.intelehealth.app.mpower.activities.dialog.CustomFragmentDialog
import org.intelehealth.app.mpower.activities.dialog.CustomPickerDialog.onInputSelected
import org.intelehealth.app.mpower.activities.patientdashboard.PatientDashboardActivity
import org.intelehealth.app.mpower.bundle.CustomDialogBundle
import org.intelehealth.app.mpower.databinding.FragmentPatientInfoBinding
import org.intelehealth.app.mpower.listeners.watcher.PatientBirthdateValidatorWatcher
import org.intelehealth.app.mpower.utilities.ImageUtils
import org.intelehealth.app.mpower.utilities.ViewUtils.getInput
//import org.intelehealth.app.mpower.utilities.ViewUtils.isCountryCodePickerEmpty
import org.intelehealth.app.mpower.utilities.ViewUtils.isEmpty
import org.intelehealth.app.mpower.utilities.makeGone
import org.intelehealth.app.mpower.utilities.makeVisible
import org.intelehealth.app.mpower.utilities.observeOnce
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.PermissionsRequester
import permissions.dispatcher.ktx.constructPermissionsRequest
import java.io.File
import java.util.Calendar

import org.joda.time.Years

@AndroidEntryPoint
class AddEditPatientFragment : BaseFragment(), onInputSelected {
    var alertDialog: AlertDialog? = null
    private var _binding: FragmentPatientInfoBinding? = null
    private val binding get() = _binding!!

    private var loadingDialog: LoadingDialogFragment? = null

    private val viewModel: AddEditPatientViewModel by viewModels()

    private lateinit var cameraAndStoragePermissions: PermissionsRequester
    private lateinit var storageWritePermission: PermissionsRequester

    private val pickPhoto = registerForActivityResult(GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        val destinationUri = Uri.fromFile(File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                ImageUtils.createUniqueImageFileName())
        )
        startCropActivity(uri, destinationUri)
    }

    private val capturePhoto = registerForActivityResult(TakePicture()) { resultOk ->
        if (!resultOk) return@registerForActivityResult
        val sourceUri = Uri.fromFile(viewModel.capturedPhotoFile)
        startCropActivity(sourceUri, sourceUri)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPatientInfoBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        setupPermissionsHandler()

        initPlaces()

        initSpinners()

        setupViewsListeners()

        fillFormFields()

        setupObservers()

        return binding.root
    }

    private fun setupPermissionsHandler() {
        cameraAndStoragePermissions = constructPermissionsRequest(
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onShowRationale = ::showCameraPermissionRationale,
                onPermissionDenied = { showSnackbarLong(R.string.permissions_camera_storage_denied) },
                onNeverAskAgain = { showSnackbarLong(R.string.permissions_camera_storage_neverask) },
                requiresPermission = ::capturePhoto
        )
        storageWritePermission = constructPermissionsRequest(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onShowRationale = { request -> request.proceed() },
                onNeverAskAgain = { showSnackbarLong(R.string.permission_storage_neverask) },
                requiresPermission = ::pickPhoto
        )
    }

    private fun showLoadingDialog() {
        loadingDialog = LoadingDialogFragment()
        loadingDialog?.show(childFragmentManager, "LoadingDialog")
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun setupObservers() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading()
                    hideSoftKeys()
                }
                is Result.Success -> if (result.operationType == PatientRegistering) {
                    hideLoading()
                    ToastUtil.success("Patient registered successfully")
                    finishActivity()
                } else {
//                    startPatientDashboardActivity()
//                    finishActivity()
                }
                is Result.Error ->
                    if (result.operationType == PatientRegistering) {
                        hideLoading()
                        if (result.throwable is NetworkUnavailableException) {
                            ToastUtil.error(result.throwable.message ?: "Network is not available")
//                            finishActivity()
                        } else {
                            ToastUtil.error("An error occurred: ${result.throwable.message}")
                        }
                        ToastUtil.error(result.throwable.message!!.toString())
                    } else {
                        hideLoading()
                        ToastUtil.error(result.throwable.message!!.toString())
                    }
                else -> throw IllegalStateException()
            }
        })

        viewModel.similarPatientsLiveData.observe(viewLifecycleOwner, Observer { similarPatients ->
            hideLoading()
            if (similarPatients.isEmpty()) registerPatient()
            else showSimilarPatientsDialog(similarPatients, viewModel.patient)
        })

        viewModel.mSearchUser.observe(viewLifecycleOwner, Observer { searchUser ->
            hideLoading()
            ToastUtil.success(String.format(getString(R.string.search_user_successful)))
            searchUser?.let { populateUserInformation(it) }
        })

        viewModel.divisionList.observe(viewLifecycleOwner, Observer { divisionList ->
            hideLoading()
            if (divisionList.isNotEmpty()){
                updateDivisionSpinner(divisionList)
            }
        })

        viewModel.districtList.observe(viewLifecycleOwner, Observer { districtList ->
            hideLoading()
            if (districtList.isNotEmpty()){
                updateDistrictSpinner(districtList)
            }
        })

        viewModel.upazilaList.observe(viewLifecycleOwner, Observer { upazilaList ->
            hideLoading()
            if (upazilaList.isNotEmpty()){
                updateUpazilaSpinner(upazilaList)
            }
        })

        viewModel.paurasavaList.observe(viewLifecycleOwner, Observer { paurasavaList ->
            hideLoading()
            if (paurasavaList.isNotEmpty()){
                updatePaurasavaSpinner(paurasavaList)
            }
        })

        viewModel.unionList.observe(viewLifecycleOwner, Observer { unionList ->
            hideLoading()
            if (unionList.isNotEmpty()){
                updateUnionSpinner(unionList)
            }
        })

        viewModel.wardList.observe(viewLifecycleOwner, Observer { wardList ->
            hideLoading()
            if (wardList.isNotEmpty()){
                updateWardSpinner(wardList)
            }
        })

        viewModel.blockList.observe(viewLifecycleOwner, Observer { blockList ->
            hideLoading()
            if (blockList.isNotEmpty()){
//                updateBlockSpinner(blockList)
            }
        })

        viewModel.mStatusOptionList.observe(viewLifecycleOwner, Observer { mStatusList ->
            hideLoading()
            if (mStatusList.isNotEmpty()){
                updateMaritalStatusSpinner()
            }
        })

        viewModel.bloodGroupOptionList.observe(viewLifecycleOwner, Observer { mStatusList ->
            hideLoading()
            if (mStatusList.isNotEmpty()){
                updateBloodGroupSpinner()
            }
        })

        viewModel.religionOptionList.observe(viewLifecycleOwner, Observer { mStatusList ->
            hideLoading()
            if (mStatusList.isNotEmpty()){
                updateReligionSpinner()
            }
        })
    }

    private fun populateUserInformation(searchUser: SearchUser) = with(binding) {
        with(searchUser){
            val mNames = fullNameEnglish?.split(" ")?.toTypedArray()
            if(mNames != null && mNames.isNotEmpty()) firstName.setText(mNames[0])
            if(mNames != null && mNames.size > 1) middlename.setText(mNames[1])
            if(mNames != null && mNames.size > 2) etFamilyName.setText(mNames[mNames.size - 1])
            etMotherName.setText(motherNameEnglish)
            etMotherNameBangla.setText(motherNameBangla)
            etFullNameBangla.setText(fullNameBangla)
            etFatherName.setText(fatherNameEnglish)
            val (day, month, year) = parseDate(dob!!)
            viewModel.dateHolder = LocalDate(year, month, day).toDateTimeAtStartOfDay()
            dobEditText.setText(String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + year)
            etMobileNo.setText(mobile)
            nationality?.let { etNationality.setText(it) }
            when(gender){
                "M" -> {
                    spinnerGender.setSelection(1)
                    viewModel.selectedGender = spinnerGender.selectedItem.toString()
                }
                "F" -> {
                    spinnerGender.setSelection(2)
                    viewModel.selectedGender = spinnerGender.selectedItem.toString()
                }
                "O" -> {
                    spinnerGender.setSelection(3)
                    viewModel.selectedGender = spinnerGender.selectedItem.toString()
                }
                else -> {
                    spinnerGender.setSelection(4)
                    viewModel.selectedGender = spinnerGender.selectedItem.toString()
                }
            }
            if(nid != null && nid!!.isNotEmpty()){
                spinnerIdentifierType.setSelection(1)
                viewModel.selectedIdentifierType = spinnerIdentifierType.selectedItem.toString()
                etSelectedIdentifierValue.setText(nid!!)
            } else if(binBrn != null && binBrn!!.isNotEmpty()){
                spinnerIdentifierType.setSelection(2)
                viewModel.selectedIdentifierType = spinnerIdentifierType.selectedItem.toString()
                etSelectedIdentifierValue.setText(binBrn!!)
            } else if(hid != null && hid!!.isNotEmpty()){
                spinnerIdentifierType.setSelection(3)
                viewModel.selectedIdentifierType = spinnerIdentifierType.selectedItem.toString()
                etSelectedIdentifierValue.setText(hid!!)
            } else {
                spinnerIdentifierType.setSelection(4)
                viewModel.selectedIdentifierType = spinnerIdentifierType.selectedItem.toString()
                etSelectedIdentifierValue.setText("")
            }
        }
    }

    private fun parseDate(dateString: String): Triple<Int, Int, Int> {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val date = LocalDate.parse(dateString, formatter)
        val day = date.dayOfMonth
        val month = date.monthOfYear
        val year = date.year

        return Triple(day, month, year)
    }

    private fun updateDivisionSpinner(divs: List<LocationData>) = with(binding.spinnerDivision) {
        val dList = arrayListOf("select division")
        divs.forEach { dList.add(it.description!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerDivision.selectedItem.equals("select division")){
                    viewModel.rxSelectedDivision = MutableLiveData()
                } else {
                    viewModel.rxSelectedDivision.value = divs[i - 1]
                    viewModel.fetchServerDistricts()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateDistrictSpinner(divs: List<LocationData>) = with(binding.spinnerDistrict) {
        val dList = arrayListOf("select district")
        divs.forEach { dList.add(it.description!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerDistrict.selectedItem.equals("select district")){
                    viewModel.rxSelectedDistrict = MutableLiveData()
                } else {
                    viewModel.rxSelectedDistrict.value = divs[i - 1]
                    viewModel.fetchServerUpazilas()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateUpazilaSpinner(divs: List<LocationData>) = with(binding.spinnerUpazilla) {
        val dList = arrayListOf("select upazila")
        divs.forEach { dList.add(it.description!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerUpazilla.selectedItem.equals("select upazila")){
                    viewModel.rxSelectedUpazila = MutableLiveData()
                } else {
                    viewModel.rxSelectedUpazila.value = divs[i - 1]
                    viewModel.fetchServerPaurasavas()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updatePaurasavaSpinner(divs: List<LocationData>) = with(binding.spinnerPaurasava) {
        val dList = arrayListOf("select paurasava")
        divs.forEach { dList.add(it.description!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerPaurasava.selectedItem.equals("select paurasava")){
                    viewModel.rxSelectedPaurasava = MutableLiveData()
                } else {
                    viewModel.rxSelectedPaurasava.value = divs[i - 1]
                    viewModel.fetchServerUnions()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateUnionSpinner(divs: List<LocationData>) = with(binding.spinnerUnion) {
        val dList = arrayListOf("select union")
        divs.forEach { dList.add(it.description!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerUnion.selectedItem.equals("select union")){
                    viewModel.rxSelectedUnion = MutableLiveData()
                } else {
                    viewModel.rxSelectedUnion.value = divs[i - 1]
                    viewModel.fetchServerWards()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateWardSpinner(divs: List<LocationData>) = with(binding.spinnerWard) {
        val dList = arrayListOf("select ward")
        divs.forEach { dList.add(it.description!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerWard.selectedItem.equals("select ward")){
                    viewModel.rxSelectedWard = MutableLiveData()
                } else {
                    viewModel.rxSelectedWard.value = divs[i - 1]
                    viewModel.fetchServerBlocks()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateBlockSpinner(divs: List<LocationData>) = with(binding.spinnerBlock) {
        val dList = arrayListOf("select block")
        divs.forEach { dList.add(it.description!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerBlock.selectedItem.equals("select block")){
                    viewModel.rxSelectedBlock = MutableLiveData()
                } else {
                    viewModel.rxSelectedBlock.value = divs[i - 1]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun findSimilarPatients() {
        validateFormInputsAndUpdateViewModel()
        viewModel.fetchSimilarPatients()
    }

    fun registerPatient() {
        validateFormInputsAndUpdateViewModel()
        if(isValidated()){
            viewModel.confirmPatient()
        }
    }

    private fun isValidated() : Boolean {
        var isValid = true
        with(binding) {
            if(textInputLayoutFirstName.isErrorEnabled ||
                textInputLayoutMiddlename.isErrorEnabled ||
                textInputLayoutSurname.isErrorEnabled ||
                textInputLayoutAddress.isErrorEnabled ||
                tilBirthPlace.isErrorEnabled ||
                tilMotherName.isErrorEnabled ||
                tilSelectedIdentifier.isErrorEnabled ||
                dobError.visibility == View.VISIBLE ||
                gendererror.visibility == View.VISIBLE){
                isValid = false
            }
        }
        return isValid
    }

    private fun updatePatient() {
        validateFormInputsAndUpdateViewModel()
        viewModel.patientUpdateLiveData.observeOnce(viewLifecycleOwner, Observer {
            val patientName = viewModel.patient.name.nameString
            when (it) {
                ResultType.PatientUpdateSuccess -> {
                    ToastUtil.success(String.format(getString(R.string.update_patient_success), patientName))
                    finishActivity()
                }
                ResultType.PatientUpdateLocalSuccess -> {
                    ToastUtil.notify(getString(R.string.offline_mode_patient_data_saved_locally_notification_message))
                    finishActivity()
                }
                else -> {
                    ToastUtil.error(String.format(getString(R.string.update_patient_error), patientName))
                    hideLoading()
                }
            }
        })
        viewModel.confirmPatient()
    }

    private fun fillFormFields() {
        if (!viewModel.isUpdatePatient) return
        with(viewModel.patient) {
            // Change to Update Patient Form
            requireActivity().title = getString(R.string.action_update_patient_data)

            binding.firstName.setText(name.givenName)
            binding.middlename.setText(name.middleName)
            binding.etFamilyName.setText(name.familyName)

            if (notNull(birthdate) || notEmpty(birthdate)) {
                viewModel.dateHolder = convertTimeString(birthdate)
                binding.dobEditText.setText(convertTime(convertTime(viewModel.dateHolder.toString(), DateUtils.OPEN_MRS_REQUEST_FORMAT)!!,
                        DateUtils.DEFAULT_DATE_FORMAT))
            }
            /*if (StringValue.MALE == gender) {
                binding.gender.check(R.id.male)
            } else if (StringValue.FEMALE == gender) {
                binding.gender.check(R.id.female)
            }*/
            binding.addressOne.setText(address.address1)
//            if (photo != null) binding.patientPhoto.setImageBitmap(resizedPhoto)
        }
    }

    private fun validateFormInputsAndUpdateViewModel() = with(binding) {

        /* Names */

        // First name validation
        if (isEmpty(firstName)) {
            textInputLayoutFirstName.isErrorEnabled = true
            textInputLayoutFirstName.error = getString(R.string.emptyerror)
            scrollToTop()
        } else if (!validateText(getInput(firstName), ILLEGAL_CHARACTERS)) {
            textInputLayoutFirstName.isErrorEnabled = true
            textInputLayoutFirstName.error = getString(R.string.fname_invalid_error)
            scrollToTop()
        } else {
            textInputLayoutFirstName.isErrorEnabled = false
        }

        // Middle name validation (can be empty)
        if (!validateText(getInput(middlename), ILLEGAL_CHARACTERS)) {
            textInputLayoutMiddlename.isErrorEnabled = true
            textInputLayoutMiddlename.error = getString(R.string.midname_invalid_error)
            scrollToTop()
        } else {
            textInputLayoutMiddlename.isErrorEnabled = false
        }

        // Family name validation
        if (isEmpty(etFamilyName)) {
            textInputLayoutSurname.isErrorEnabled = true
            textInputLayoutSurname.error = getString(R.string.emptyerror)
            scrollToTop()
        } else if (!validateText(getInput(etFamilyName), ILLEGAL_CHARACTERS)) {
            textInputLayoutSurname.isErrorEnabled = true
            textInputLayoutSurname.error = getString(R.string.lname_invalid_error)
            scrollToTop()
        } else {
            textInputLayoutSurname.isErrorEnabled = false
        }

        viewModel.patient.names = listOf(PersonName().apply {
            givenName = getInput(firstName)
            middleName = getInput(middlename)
            familyName = getInput(etFamilyName)
        })

        /* Gender */
        /*val genderChoices = arrayOf(StringValue.MALE, StringValue.FEMALE)
        val index = gender.indexOfChild(requireActivity().findViewById(gender.checkedRadioButtonId))
        if (index == -1) {
            gendererror.makeVisible()
            scrollToTop()
            viewModel.patient.gender = null
        } else {
            gendererror.makeGone()
            viewModel.patient.gender = genderChoices[index]
        }*/

        /* Addresses */


        if (isEmpty(etBirthPlace)) {
            tilBirthPlace.isErrorEnabled = true
            tilBirthPlace.error = getString(R.string.empty_birth_place)
            scrollToTop()
        } else {
            tilBirthPlace.isErrorEnabled = false
            tilBirthPlace.error = ""
        }

        if (isEmpty(etMotherName)) {
            tilMotherName.isErrorEnabled = true
            tilMotherName.error = getString(R.string.empty_mother_name)
            scrollToTop()
        } else {
            tilMotherName.isErrorEnabled = false
            tilMotherName.error = ""
        }

        if (isEmpty(etSelectedIdentifierValue)) {
            tilSelectedIdentifier.isErrorEnabled = true
            tilSelectedIdentifier.error = getString(R.string.empty_nid)
            scrollToTop()
        } else {
            tilSelectedIdentifier.isErrorEnabled = false
            tilSelectedIdentifier.error = ""
        }

        if (isEmpty(addressOne)) {
            addressError.makeVisible()
            addressError.text = getString(R.string.atleastone)
            textInputLayoutAddress.error = getString(R.string.atleastone)
            scrollToTop()
        } else if (!validateText(getInput(addressOne), ILLEGAL_ADDRESS_CHARACTERS)) {
            addressError.makeVisible()
            addressError.text = getString(R.string.addr_invalid_error)
            scrollToTop()
            if (!validateText(getInput(addressOne), ILLEGAL_ADDRESS_CHARACTERS)) textInputLayoutAddress.error = getString(R.string.addr_invalid_error)
            else textInputLayoutAddress.isErrorEnabled = false
        } else {
            addressError.makeGone()
            textInputLayoutAddress.isErrorEnabled = false
        }

        viewModel.patient.addresses = listOf(PersonAddress().apply {
            address1 = getInput(addressOne)
            preferred = true
        })


        /* Birth date */
        if (isEmpty(dobEditText)) {
            if (isBlank(getInput(estimatedYear)) && isBlank(getInput(estimatedMonth))) {
                val dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT)
                val minimumDate = DateTime.now().minusYears(
                        ApplicationConstants.RegisterPatientRequirements.MAX_PATIENT_AGE)
                        .toString(dateTimeFormatter)
                val maximumDate = DateTime.now().toString(dateTimeFormatter)
                dobError.text = getString(R.string.dob_error, minimumDate, maximumDate)
                dobError.makeVisible()
                scrollToTop()
            } else {
                viewModel.patient.birthdateEstimated = true
                val yearDiff = if (isEmpty(estimatedYear)) 0 else estimatedYear.text.toString().toInt()
                val monthDiff = if (isEmpty(estimatedMonth)) 0 else estimatedMonth.text.toString().toInt()
                viewModel.dateHolder = getDateTimeFromDifference(yearDiff, monthDiff)
                dobError.makeGone()
            }
        } else {
            viewModel.patient.birthdateEstimated = false
            val insertedDate = dobEditText.text.toString().trim { it <= ' ' }
            val minDateOfBirth = DateTime.now().minusYears(
                    ApplicationConstants.RegisterPatientRequirements.MAX_PATIENT_AGE)
            val maxDateOfBirth = DateTime.now()
            if (validateDate(insertedDate, minDateOfBirth, maxDateOfBirth)) {
                val dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT)
                viewModel.dateHolder = dateTimeFormatter.parseDateTime(insertedDate)
            }
            dobError.makeGone()
        }
        var mGender = ""
        gendererror.makeGone()
        when(viewModel.selectedGender){
            "Male" -> {
                mGender = "M"
            }
            "Female" -> {
                mGender = "F"
            }
            "Other" -> {
                mGender = "O"
            }
            else -> {
                gendererror.makeVisible()
                scrollToTop()
            }
        }
        viewModel.patient.gender = mGender
        viewModel.patient.birthdate = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT).print(viewModel.dateHolder)
        viewModel.patient.age = calculateAge(viewModel.patient.birthdate)

        val mPerson = Person()
        mPerson.names = viewModel.patient.names
        mPerson.gender = viewModel.patient.gender
        mPerson.age = viewModel.patient.age
        mPerson.birthdate = viewModel.patient.birthdate
        mPerson.display = viewModel.patient.display

        viewModel.patient.person = mPerson

        viewModel.customAttrList = setCustomPatientAttributes()
        viewModel.localAttrList = setPatientAttributes()
        println()
    }

    fun calculateAge(birthDate: String): Int {
        val birthLocalDate = LocalDate.parse(birthDate)
        val currentDate = LocalDate.now()
        val age = Years.yearsBetween(birthLocalDate, currentDate).years
        return age
    }

    private fun setCustomPatientAttributes() : MutableList<PersonAttributeCustom>{
        val attrValues : MutableList<PersonAttributeCustom> = mutableListOf()


        if(binding.etSelectedIdentifierValue.text.toString().isNotEmpty()){
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
    }

    private fun setPatientAttributes() : MutableList<PersonAttribute>{
        val attrValues : MutableList<PersonAttribute> = mutableListOf()

        if(binding.etSelectedIdentifierValue.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.uuid = ApplicationConstants.PATIENTS_NID_UUID
            pa.display = ApplicationConstants.PATIENTS_NID_KEY
            pa.value = binding.etSelectedIdentifierValue.text.toString()
            attrValues.add(pa)
        }
        if(binding.etBirthRegNumber.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.uuid = ApplicationConstants.PATIENTS_BRID_UUID
            pa.display = ApplicationConstants.PATIENTS_BRID_KEY
            pa.value = binding.etBirthRegNumber.text.toString()
            attrValues.add(pa)
        }
        if(binding.etMobileNo.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_MOBILE_KEY
            pa.uuid = ApplicationConstants.PATIENTS_MOBILE_UUID
            pa.value = binding.etMobileNo.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFullNameBangla.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_FULL_NAME_BANGLA_KEY
            pa.uuid = ApplicationConstants.PATIENTS_FULL_NAME_BANGLA_UUID
            pa.value = binding.etFullNameBangla.text.toString()
            attrValues.add(pa)
        }
        if(binding.etMotherName.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_MOTHER_NAME_KEY
            pa.uuid = ApplicationConstants.PATIENTS_MOTHER_NAME_UUID
            pa.value = binding.etMotherName.text.toString()
            attrValues.add(pa)
        }
        if(binding.etMotherNameBangla.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_MOTHER_NAME_BANGLA_KEY
            pa.uuid = ApplicationConstants.PATIENTS_MOTHER_NAME_BANGLA_UUID
            pa.value = binding.etMotherNameBangla.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFatherName.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_FATHER_NAME_KEY
            pa.uuid = ApplicationConstants.PATIENTS_FATHER_NAME_UUID
            pa.value = binding.etFatherName.text.toString()
            attrValues.add(pa)
        }
        if(binding.etFatherNameBangla.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_FATHER_NAME_BANGLA_KEY
            pa.uuid = ApplicationConstants.PATIENTS_FATHER_NAME_BANGLA_UUID
            pa.value = binding.etFatherNameBangla.text.toString()
            attrValues.add(pa)
        }
        if(binding.etBirthPlace.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_BIRTH_PLACE_KEY
            pa.uuid = ApplicationConstants.PATIENTS_BIRTH_PLACE_UUID
            pa.value = binding.etBirthPlace.text.toString()
            attrValues.add(pa)
        }
        if(binding.etNationality.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_NATIONALITY_KEY
            pa.uuid = ApplicationConstants.PATIENTS_NATIONALITY_UUID
            pa.value = binding.etNationality.text.toString()
            attrValues.add(pa)
        }
        if(binding.etOccupation.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_OCCUPATION_KEY
            pa.uuid = ApplicationConstants.PATIENTS_OCCUPATION_UUID
            pa.value = binding.etOccupation.text.toString()
            attrValues.add(pa)
        }
        if(binding.etEducation.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_EDUCATION_KEY
            pa.uuid = ApplicationConstants.PATIENTS_EDUCATION_UUID
            pa.value = binding.etEducation.text.toString()
            attrValues.add(pa)
        }
        if(binding.etDisabilityType.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_DISABILITY_TYPE_KEY
            pa.uuid = ApplicationConstants.PATIENTS_DISABILITY_TYPE_UUID
            pa.value = binding.etDisabilityType.text.toString()
            attrValues.add(pa)
        }
        if(binding.etEthnicity.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_ETHNICITY_KEY
            pa.uuid = ApplicationConstants.PATIENTS_ETHNICITY_UUID
            pa.value = binding.etEthnicity.text.toString()
            attrValues.add(pa)
        }
        if(binding.addressOne.text.toString().isNotEmpty()){
            val pa = PersonAttribute()
            pa.display = ApplicationConstants.PATIENTS_ADDRESS_KEY
            pa.uuid = ApplicationConstants.PATIENTS_ADDRESS_UUID
            pa.value = binding.addressOne.text.toString()
            attrValues.add(pa)
        }
        if(viewModel.rxSelectedDivision.value?.locationId != null){
            for (ld in viewModel.divisionList.value!!){
                if(ld.locationId == viewModel.rxSelectedDivision.value?.locationId){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_DIVISION_UUID
                    pa.display = ApplicationConstants.PATIENTS_DIVISION_KEY
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttribute()
                    pa2.uuid = ApplicationConstants.PATIENTS_DIVISION_ID_UUID
                    pa2.display = ApplicationConstants.PATIENTS_DIVISION_ID_KEY
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedDistrict.value?.locationId != null){
            for (ld in viewModel.districtList.value!!){
                if(ld.locationId == viewModel.rxSelectedDistrict.value?.locationId){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_DISTRICT_UUID
                    pa.display = ApplicationConstants.PATIENTS_DISTRICT_KEY
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttribute()
                    pa2.uuid = ApplicationConstants.PATIENTS_DISTRICT_ID_UUID
                    pa2.display = ApplicationConstants.PATIENTS_DISTRICT_ID_KEY
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedUpazila.value?.locationId != null){
            for (ld in viewModel.upazilaList.value!!){
                if(ld.locationId == viewModel.rxSelectedUpazila.value?.locationId){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_UPAZILA_UUID
                    pa.display = ApplicationConstants.PATIENTS_UPAZILA_KEY
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttribute()
                    pa2.uuid = ApplicationConstants.PATIENTS_UPAZILA_ID_UUID
                    pa2.display = ApplicationConstants.PATIENTS_UPAZILA_ID_KEY
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedPaurasava.value?.locationId != null){
            for (ld in viewModel.paurasavaList.value!!){
                if(ld.locationId == viewModel.rxSelectedPaurasava.value?.locationId){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_PAURASAVA_UUID
                    pa.display = ApplicationConstants.PATIENTS_PAURASAVA_KEY
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttribute()
                    pa2.uuid = ApplicationConstants.PATIENTS_PAURASAVA_ID_UUID
                    pa2.display = ApplicationConstants.PATIENTS_PAURASAVA_ID_KEY
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedUnion.value?.locationId != null){
            for (ld in viewModel.unionList.value!!){
                if(ld.locationId == viewModel.rxSelectedUnion.value?.locationId){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_UNION_UUID
                    pa.display = ApplicationConstants.PATIENTS_UNION_KEY
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttribute()
                    pa2.uuid = ApplicationConstants.PATIENTS_UNION_ID_UUID
                    pa2.display = ApplicationConstants.PATIENTS_UNION_ID_KEY
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.rxSelectedWard.value?.locationId != null){
            for (ld in viewModel.wardList.value!!){
                if(ld.locationId == viewModel.rxSelectedWard.value?.locationId){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_WARD_UUID
                    pa.display = ApplicationConstants.PATIENTS_WARD_KEY
                    pa.value = ld.description
                    attrValues.add(pa)
                    val pa2 = PersonAttribute()
                    pa2.uuid = ApplicationConstants.PATIENTS_WARD_ID_UUID
                    pa2.display = ApplicationConstants.PATIENTS_WARD_ID_KEY
                    pa2.value = ld.locationId.toString()
                    attrValues.add(pa2)
                }
            }
        }
        if(viewModel.selectedMaritalStatusOption.uuid != null){
            for (ld in viewModel.mStatusOptionList.value!!){
                if(ld.uuid == viewModel.selectedMaritalStatusOption.uuid){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_MARITAL_STATUS_UUID
                    pa.display = ApplicationConstants.PATIENTS_MARITAL_STATUS_KEY
                    pa.value = ld.uuid
                    attrValues.add(pa)
                }
            }
        }
        if(viewModel.selectedBloodGroupOption.uuid != null){
            for (ld in viewModel.bloodGroupOptionList.value!!){
                if(ld.uuid == viewModel.selectedBloodGroupOption.uuid){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_BLOOD_GROUP_UUID
                    pa.display = ApplicationConstants.PATIENTS_BLOOD_GROUP_KEY
                    pa.value = ld.uuid
                    attrValues.add(pa)
                }
            }
        }
        if(viewModel.selectedReligionOption.uuid != null){
            for (ld in viewModel.religionOptionList.value!!){
                if(ld.uuid == viewModel.selectedReligionOption.uuid){
                    val pa = PersonAttribute()
                    pa.uuid = ApplicationConstants.PATIENTS_RELIGION_UUID
                    pa.display = ApplicationConstants.PATIENTS_RELIGION_KEY
                    pa.value = ld.uuid
                    attrValues.add(pa)
                }
            }
        }

        return attrValues
    }


    private fun showSimilarPatientsDialog(patients: List<Patient>, patient: Patient) {
        CustomDialogBundle().apply {
            titleViewMessage = getString(R.string.similar_patients_dialog_title)
            rightButtonText = getString(R.string.dialog_button_register_new)
            rightButtonAction = CustomFragmentDialog.OnClickAction.REGISTER_PATIENT
            leftButtonText = getString(R.string.dialog_button_cancel)
            leftButtonAction = CustomFragmentDialog.OnClickAction.DISMISS
            patientsList = patients
            newPatient = patient
        }.let {
            (requireActivity() as AddEditPatientActivity)
                    .createAndShowDialog(it, ApplicationConstants.DialogTAG.SIMILAR_PATIENTS_TAG)
        }
    }

    private fun setupViewsListeners() = with(binding) {

//        gender.setOnCheckedChangeListener { _, _ -> gendererror.makeGone() }

        dobEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // No need for this method
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Auto-add slash before entering month (e.g. "17/*") and before entering year (e.g. "17/10/*")
                dobEditText.text.toString().let {
                    if ((it.length == 3 && !it.contains("/")) ||
                            (it.length == 6 && !it.substring(3).contains("/"))) {
                        dobEditText.setText(StringBuilder(it).insert(it.length - 1, "/").toString())
                        dobEditText.setSelection(dobEditText.text.length)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                // If a considerable amount of text is filled in dobEditText, then remove 'Estimated age' fields.
                if (s.length >= 10) {
                    estimatedMonth.text.clear()
                    estimatedYear.text.clear()
                }
            }
        })

        btnIdentifierSearch.setOnClickListener {
//            viewModel.onSearch(SearchRequest(spinnerIdentifier.selectedItem.toString().toLowerCase(), binding.etIdentifierDOB.text.toString(), binding.etIdentifierValue.text.toString()))
            viewModel.onSearch(SearchRequest("nid", "2000-02-18", "6915166281"))
        }

        btnIdentifierDOB.setOnClickListener {
            val cYear: Int
            val cMonth: Int
            val cDay: Int
            if (viewModel.identifierDateHolder == null) Calendar.getInstance().let {
                cYear = it[Calendar.YEAR]
                cMonth = it[Calendar.MONTH]
                cDay = it[Calendar.DAY_OF_MONTH]
            } else viewModel.identifierDateHolder!!.run {
                cYear = year
                cMonth = monthOfYear - 1
                cDay = dayOfMonth
            }

            val dateSetListener = { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val adjustedMonth = selectedMonth + 1
//                etIdentifierDOB.setText(String.format("%02d", selectedDay) + "-" + String.format("%02d", adjustedMonth) + "-" + selectedYear)
                etIdentifierDOB.setText(selectedYear.toString() + "-" + String.format("%02d", adjustedMonth) + "-" + String.format("%02d", selectedDay))
                viewModel.identifierDateHolder = LocalDate(selectedYear, adjustedMonth, selectedDay).toDateTimeAtStartOfDay()
            }
            DatePickerDialog(requireActivity(), dateSetListener, cYear, cMonth, cDay).apply {
                datePicker.maxDate = System.currentTimeMillis()
                setTitle(getString(R.string.date_picker_title))
            }.show()
        }

        datePicker.setOnClickListener {
            val cYear: Int
            val cMonth: Int
            val cDay: Int
            if (viewModel.dateHolder == null) Calendar.getInstance().let {
                cYear = it[Calendar.YEAR]
                cMonth = it[Calendar.MONTH]
                cDay = it[Calendar.DAY_OF_MONTH]
            } else viewModel.dateHolder!!.run {
                cYear = year
                cMonth = monthOfYear - 1
                cDay = dayOfMonth
            }
            estimatedMonth.text.clear()
            estimatedYear.text.clear()

            val dateSetListener = { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val adjustedMonth = selectedMonth + 1
                dobEditText.setText(String.format("%02d", selectedDay) + "/" + String.format("%02d", adjustedMonth) + "/" + selectedYear)
                viewModel.dateHolder = LocalDate(selectedYear, adjustedMonth, selectedDay).toDateTimeAtStartOfDay()
            }
            DatePickerDialog(requireActivity(), dateSetListener, cYear, cMonth, cDay).apply {
                datePicker.maxDate = System.currentTimeMillis()
                setTitle(getString(R.string.date_picker_title))
            }.show()
        }

        PatientBirthdateValidatorWatcher(requireContext(), dobEditText, estimatedMonth, estimatedYear).let {
            estimatedMonth.addTextChangedListener(it)
            estimatedYear.addTextChangedListener(it)
        }

        /*capturePhoto.setOnClickListener {
            val dialogList = mutableListOf(
                    CustomDialogModel(getString(R.string.dialog_take_photo), R.drawable.ic_photo_camera),
                    CustomDialogModel(getString(R.string.dialog_choose_photo), R.drawable.ic_photo_library)
            )
            if (viewModel.patient.photo != null) {
                dialogList.add(CustomDialogModel(getString(R.string.dialog_remove_photo), R.drawable.ic_photo_delete))
            }
            CustomPickerDialog(dialogList)
                    .apply { setTargetFragment(this@AddEditPatientFragment, 1000) }
                    .show(requireFragmentManager(), "tag")
        }

        patientPhoto.setOnClickListener {
            if (viewModel.capturedPhotoFile != null) {
                val i = Intent(Intent.ACTION_VIEW)
                i.setDataAndType(Uri.fromFile(viewModel.capturedPhotoFile), ApplicationConstants.IMAGE_JPEG)
                startActivity(i)
            } else if (viewModel.patient.photo != null) {
                viewModel.patient.run { ImageUtils.showPatientPhoto(requireContext(), photo, name.nameString) }
            }
        }*/

    }

    private fun initPlaces() {
        /*if (viewModel.placesClient != null) return
        with(requireActivity()) {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val placesApiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY")
            if (!Places.isInitialized() && placesApiKey != null) {
                Places.initialize(applicationContext, placesApiKey)
                viewModel.placesClient = Places.createClient(this)
            }
        }*/
    }

    private fun initSpinners() {
        viewModel.fetchServerDivisions()
        viewModel.fetchMaritalStatusOptions()
        viewModel.fetchBloodGroupOptions()
        viewModel.fetchReligionOptions()

        updateGenderSpinner()
        updateIdentifierSpinner()
        updateIdentifierTypeSpinner()
    }

    private fun updateGenderSpinner() = with(binding.spinnerGender) {
        val dList = arrayListOf("select gender")
        viewModel.genderList.forEach { dList.add(it) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerGender.selectedItem.equals("select gender")){
                    viewModel.selectedGender = ""
                } else {
                    viewModel.selectedGender = viewModel.genderList[i - 1]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateIdentifierSpinner() = with(binding.spinnerIdentifier) {
        val dList = arrayListOf("select ID")
        viewModel.mIdentifierList.forEach { dList.add(it) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        setSelection(0)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerIdentifier.selectedItem.equals("select ID")){
                    viewModel.selectedIdentifier = ""
                } else {
                    viewModel.selectedIdentifier = viewModel.mIdentifierList[i - 1]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateIdentifierTypeSpinner() = with(binding.spinnerIdentifierType) {
        val dList = arrayListOf("select identifier type")
        viewModel.mIdentifierTypeList.forEach { dList.add(it) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        setSelection(0)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerIdentifierType.selectedItem.equals("select identifier type")){
                    viewModel.selectedIdentifierType = ""
                } else {
                    viewModel.selectedIdentifierType = viewModel.mIdentifierTypeList[i - 1]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateMaritalStatusSpinner() = with(binding.spinnerMaritalStatus) {
        val dList = arrayListOf("select marital status")
        viewModel.mStatusOptionList.value?.forEach { dList.add(it.display!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerMaritalStatus.selectedItem.equals("select marital status")){
                    viewModel.selectedMaritalStatusOption = ConceptOption()
                } else {
                    viewModel.selectedMaritalStatusOption = viewModel.mStatusOptionList.value?.get(i - 1)!!
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateBloodGroupSpinner() = with(binding.spinnerBloodGroup) {
        val dList = arrayListOf("select blood group")
        viewModel.bloodGroupOptionList.value?.forEach { dList.add(it.display!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerBloodGroup.selectedItem.equals("select blood group")){
                    viewModel.selectedBloodGroupOption = ConceptOption()
                } else {
                    viewModel.selectedBloodGroupOption = viewModel.bloodGroupOptionList.value?.get(i - 1)!!
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun updateReligionSpinner() = with(binding.spinnerReligion) {
        val dList = arrayListOf("select religion")
        viewModel.religionOptionList.value?.forEach { dList.add(it.display!!) }
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dList).also { adapter = it }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if(binding.spinnerReligion.selectedItem.equals("select religion")){
                    viewModel.selectedReligionOption = ConceptOption()
                } else {
                    viewModel.selectedReligionOption = viewModel.religionOptionList.value?.get(i - 1)!!
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    override fun performFunction(position: Int) = when (position) {
        0 -> {
            // Capture photo
            StrictMode.VmPolicy.Builder().run { StrictMode.setVmPolicy(build()) }
            cameraAndStoragePermissions.launch()
        }
        1 -> {
            // Pick photo from gallery
            storageWritePermission.launch()
        }
        2 -> {
            // Remove photo
//            binding.patientPhoto.setImageResource(R.drawable.ic_person_grey_500_48dp)
//            binding.patientPhoto.invalidate()
            viewModel.patient.photo = null
        }
        else -> {
        }
    }

    private fun capturePhoto() = with(viewModel) {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        capturedPhotoFile = File(dir, ImageUtils.createUniqueImageFileName())
        capturePhoto.launch(Uri.fromFile(capturedPhotoFile))
    }

    private fun pickPhoto() = pickPhoto.launch(URI_IMAGE)

    private fun showCameraPermissionRationale(request: PermissionRequest) {
        AlertDialog.Builder(requireActivity())
                .setMessage(R.string.permissions_camera_storage_rationale)
                .setPositiveButton(R.string.button_allow) { _: DialogInterface?, _: Int -> request.proceed() }
                .setNegativeButton(R.string.button_deny) { _: DialogInterface?, _: Int -> request.cancel() }
                .show()
    }

    private fun showLoading() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.transpScreenScreen.makeVisible()
        binding.progressBar.makeVisible()
    }

    private fun hideLoading() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.transpScreenScreen.makeGone()
        binding.progressBar.makeGone()
    }

    fun isLoading(): Boolean = viewModel.result.value is Result.Loading

    private fun showSnackbarLong(stringId: Int) {
        Snackbar.make(binding.addEditConstraintLayout, stringId, Snackbar.LENGTH_LONG)
                .apply {
                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            .setTextColor(Color.WHITE)
                }
                .show()
    }

    private fun submitAction() = with(viewModel) {
        // New patient registering
        if (!isUpdatePatient) {
//            findSimilarPatients()
            registerPatient()
            return@with
        }
        // Existing patient updating
        if (patient.isDeceased && !patient.causeOfDeath.uuid.isNullOrEmpty()) {
            alertDialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                    .setTitle(R.string.mark_patient_deceased)
                    .setMessage(R.string.mark_patient_deceased_notice)
                    .setCancelable(false)
                    .setPositiveButton(R.string.mark_patient_deceased_proceed) { _, _ ->
                        alertDialog?.cancel()
                        updatePatient()
                    }
                    .setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
                        alertDialog?.cancel()
                    }
                    .create()
            alertDialog?.show()
        } else {
            updatePatient()
        }
    }

    private fun resetAction() = with(binding) {
        firstName.setText("")
        middlename.setText("")
        etFamilyName.setText("")
        dobEditText.setText("")
        estimatedYear.setText("")
        estimatedMonth.setText("")
        addressOne.setText("")
//        gender.clearCheck()
        dobError.text = ""
        gendererror.makeGone()
        addressError.text = ""
        textInputLayoutFirstName.error = ""
        textInputLayoutMiddlename.error = ""
        textInputLayoutSurname.error = ""
        textInputLayoutAddress.error = ""
//        patientPhoto.setImageResource(R.drawable.ic_person_grey_500_48dp)
        viewModel.resetPatient()
    }

    private fun scrollToTop() = binding.run { scrollView.smoothScrollTo(0, scrollView.paddingTop) }

    private fun hideSoftKeys() {
        requireActivity().let {
            val view = it.currentFocus ?: View(it)
            val inputMethodManager = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun isAnyFieldNotEmpty(): Boolean = with(binding) {
        return !isEmpty(firstName) || !isEmpty(middlename) || !isEmpty(etFamilyName) ||
                !isEmpty(dobEditText) || !isEmpty(estimatedYear) || !isEmpty(estimatedMonth) ||
                !isEmpty(addressOne)
    }


    private fun startCropActivity(sourceUri: Uri, destinationUri: Uri) {
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(ApplicationConstants.ASPECT_RATIO_FOR_CROPPING, ApplicationConstants.ASPECT_RATIO_FOR_CROPPING)
                .start(requireActivity(), this@AddEditPatientFragment)
    }

    private fun startPatientDashboardActivity() {
        Intent(requireActivity(), PatientDashboardActivity::class.java).apply {
            putExtra(PATIENT_ID_BUNDLE, viewModel.patient.id)
            startActivity(this)
        }
    }

    private fun finishActivity() = requireActivity().finish()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                data?.let { UCrop.getOutput(it) }?.path?.let {
                    viewModel.patient.photo = ImageUtils.getResizedPortraitImage(it)
                    /*with(binding.patientPhoto) {
                        val bitmap = ThumbnailUtils.extractThumbnail(viewModel.patient.photo, width, height)
                        setImageBitmap(bitmap)
                        invalidate()
                    }*/
                }
            } else {
                viewModel.capturedPhotoFile = null
                data?.let { ToastUtil.error(UCrop.getError(it)?.message!!) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.submit_done_menu, menu)
        if (viewModel.isUpdatePatient) {
            // Remove reset button when updating a patient
            menu.findItem(R.id.actionReset).run {
                isVisible = false
                isEnabled = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
            R.id.actionSubmit -> submitAction()
            R.id.actionReset -> AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.dialog_title_reset_patient)
                    .setMessage(R.string.reset_dialog_message)
                    .setPositiveButton(R.string.dialog_button_ok) { dialogInterface: DialogInterface?, i: Int -> resetAction() }
                    .setNegativeButton(R.string.dialog_button_cancel, null)
                    .show()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(StringValue.MALE, StringValue.FEMALE)
    annotation class StringValue {
        companion object {
            const val FEMALE = "F"
            const val MALE = "M"
        }
    }

    companion object {
        fun newInstance(patientID: String?, countries: List<String>) = AddEditPatientFragment().apply {
            arguments = bundleOf(
                    Pair(PATIENT_ID_BUNDLE, patientID),
                    Pair(COUNTRIES_BUNDLE, countries)
            )
        }
    }
}
