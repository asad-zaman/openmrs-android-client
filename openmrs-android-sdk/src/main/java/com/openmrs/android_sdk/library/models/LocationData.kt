package com.openmrs.android_sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

class AllLocation : Serializable {

    @SerializedName("locations")
    @Expose
    var locationList: List<LocationData> = ArrayList()

}

class LocationData : Serializable {

    @SerializedName("location_id")
    @Expose
    var locationId: Int? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("parent_location")
    @Expose
    var parentLocation: Int? = null

    @SerializedName("location_tag_id")
    @Expose
    var locationTagId: Int? = null

}