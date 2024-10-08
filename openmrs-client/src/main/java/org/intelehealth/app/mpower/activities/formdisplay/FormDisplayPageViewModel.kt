package org.intelehealth.app.mpower.activities.formdisplay

import androidx.lifecycle.SavedStateHandle
import com.openmrs.android_sdk.library.models.Page
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.FORM_FIELDS_BUNDLE
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.FORM_PAGE_BUNDLE
import com.openmrs.android_sdk.utilities.InputField
import com.openmrs.android_sdk.utilities.SelectOneField
import dagger.hilt.android.lifecycle.HiltViewModel
import org.intelehealth.app.mpower.activities.BaseViewModel
import org.intelehealth.app.mpower.bundle.FormFieldsWrapper
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class FormDisplayPageViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : BaseViewModel<Unit>() {

    val page: Page = savedStateHandle.get(FORM_PAGE_BUNDLE)!!
    var inputFields = mutableListOf<InputField>()
    var selectOneFields = mutableListOf<SelectOneField>()

    init {
        val formFieldsWrapper: FormFieldsWrapper? = savedStateHandle.get(FORM_FIELDS_BUNDLE)
        formFieldsWrapper?.let {
            inputFields = it.inputFields as MutableList
            selectOneFields = it.selectOneFields as MutableList
        }
    }

    fun getOrCreateInputField(concept: String, mRequired: Boolean): InputField {
        var inputField = findInputFieldByConcept(concept)
        if (inputField == null) {
            inputField = InputField(concept, mRequired)
            inputFields.add(inputField)
        }
        return inputField
    }

    fun findInputFieldByConcept(concept: String): InputField? {
        inputFields.forEach {
            if (it.id == abs(concept.hashCode())) return it
        }
        return null
    }

    fun findSelectOneFieldById(concept: String): SelectOneField? {
        selectOneFields.forEach {
            if (it.concept == concept) return it
        }
        return null
    }
}
