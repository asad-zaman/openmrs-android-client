package com.openmrs.android_sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

data class ConceptApiResponse (
    @SerializedName("uuid")
    @Expose
    var uuid : String? = null,

    @SerializedName("display")
    @Expose
    var display : String? = null,

    @SerializedName("answers")
    @Expose
    var answers : List<ConceptOption> = listOf()
)

data class ConceptOption (
    @SerializedName("uuid")
    @Expose
    var uuid : String? = null,

    @SerializedName("display")
    @Expose
    var display : String? = null
)