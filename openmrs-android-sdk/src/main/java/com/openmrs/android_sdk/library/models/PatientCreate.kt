package com.openmrs.android_sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PatientCreate {

    @SerializedName("uuid")
    @Expose
    var uuid: String? = null

    @SerializedName("person")
    @Expose
    var person: Person? = null

    @SerializedName("identifiers")
    @Expose
    var identifiers: List<PatientIdentifier>? = listOf()

}