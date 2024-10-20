/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package com.openmrs.android_sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class SearchUserResponse : Serializable {

    @SerializedName("personInformation")
    @Expose
    var searchUser: SearchUser? = null

}

class SearchUser : Serializable {

    @SerializedName("citizenNid")
    @Expose
    var nid: String? = null

    @SerializedName("hid")
    @Expose
    var hid: String? = null

    @SerializedName("binBrn")
    @Expose
    var binBrn: String? = null

    @SerializedName("dob")
    @Expose
    var dob: String? = null

    @SerializedName("mobile")
    @Expose
    var mobile: String? = null

    @SerializedName("gender")
    @Expose
    var gender: String? = null

    @SerializedName("nationality")
    @Expose
    var nationality: String? = null

    @SerializedName("fullNameEnglish")
    @Expose
    var fullNameEnglish: String? = null

    @SerializedName("fullNameBangla")
    @Expose
    var fullNameBangla: String? = null

    @SerializedName("motherNameEnglish")
    @Expose
    var motherNameEnglish: String? = null

    @SerializedName("motherNameBangla")
    @Expose
    var motherNameBangla: String? = null

    @SerializedName("fatherNameEnglish")
    @Expose
    var fatherNameEnglish: String? = null

    /*@SerializedName("fatherNameBangla")
    @Expose
    var fatherNameBangla: Int? = null*/

}




data class SearchRequest(
    @SerializedName("type") @Expose var rtype: String?,
    @SerializedName("dob") @Expose var rdob: String?,
    @SerializedName("text") @Expose var rtext: String?
) : Serializable


data class CallTokenModel(
    @SerializedName("name") @Expose var did: String,
    @SerializedName("roomId") @Expose var tid: String,
    @SerializedName("nurseName") @Expose var pid: String
) : Serializable

data class RTCToken(
    @SerializedName("token") @Expose var token: String?,
    @SerializedName("appToken") @Expose var appToken: String?,
    @SerializedName("success") @Expose var success: Boolean?
) : Serializable
