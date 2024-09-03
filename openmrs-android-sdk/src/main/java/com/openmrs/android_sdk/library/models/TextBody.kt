package com.openmrs.android_sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TextBody(
    @SerializedName("txt") @Expose var txt: String?
) : Serializable