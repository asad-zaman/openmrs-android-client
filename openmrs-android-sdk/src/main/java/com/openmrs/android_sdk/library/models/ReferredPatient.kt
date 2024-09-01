package com.openmrs.android_sdk.library.models

import com.google.gson.annotations.SerializedName

data class ReferredPatientResponse(
    @SerializedName("persons") val persons: List<ReferredPatient>
)

data class ReferredPatient (

    @SerializedName("id"             ) var id             : Int?     = null,
    @SerializedName("person"         ) var person         : Int?     = null,
    @SerializedName("personUuid"     ) var personUuid     : String?  = null,
    @SerializedName("gender"         ) var gender         : String?  = null,
    @SerializedName("birthdate"      ) var birthdate      : Int?     = null,
    @SerializedName("firstName"      ) var firstName      : String?  = null,
    @SerializedName("lastName"       ) var lastName       : String?  = null,
    @SerializedName("identifier"     ) var identifier     : String?  = null,
    @SerializedName("nid"            ) var nid            : String?  = null,
    @SerializedName("uuid"           ) var uuid           : String?  = null,
    @SerializedName("brn"            ) var brn            : String?  = null,
    @SerializedName("epi"            ) var epi            : String?  = null,
    @SerializedName("mobile"         ) var mobile         : String?  = null,
    @SerializedName("motherName"     ) var motherName     : String?  = null,
    @SerializedName("shrId"          ) var shrId          : String?  = null,
    @SerializedName("highRisk"       ) var highRisk       : Boolean? = null,
    @SerializedName("fatherName"     ) var fatherName     : String?  = null,
    @SerializedName("spouseName"     ) var spouseName     : String?  = null,
    @SerializedName("refered"        ) var refered        : Boolean? = null,
    @SerializedName("location"       ) var location       : String?  = null,
    @SerializedName("country"        ) var country        : String?  = null,
    @SerializedName("division"       ) var division       : String?  = null,
    @SerializedName("district"       ) var district       : String?  = null,
    @SerializedName("upazila"        ) var upazila        : String?  = null,
    @SerializedName("paurasava"      ) var paurasava      : String?  = null,
    @SerializedName("union"          ) var union          : String?  = null,
    @SerializedName("ward"           ) var ward           : String?  = null,
    @SerializedName("block"          ) var block          : String?  = null,
    @SerializedName("occupation"     ) var occupation     : String?  = null,
    @SerializedName("relegion"       ) var relegion       : String?  = null,
    @SerializedName("bloodGroup"     ) var bloodGroup     : String?  = null,
    @SerializedName("ethnicity"      ) var ethnicity      : String?  = null,
    @SerializedName("nationality"    ) var nationality    : String?  = null,
    @SerializedName("matritalStatus" ) var matritalStatus : String?  = null,
    @SerializedName("countryId"      ) var countryId      : String?  = null,
    @SerializedName("divisionId"     ) var divisionId     : String?  = null,
    @SerializedName("districtId"     ) var districtId     : String?  = null,
    @SerializedName("upazilaId"      ) var upazilaId      : String?  = null,
    @SerializedName("paurasavaId"    ) var paurasavaId    : String?  = null,
    @SerializedName("unionId"        ) var unionId        : String?  = null,
    @SerializedName("wardId"         ) var wardId         : String?  = null,
    @SerializedName("blockId"        ) var blockId        : String?  = null,
    @SerializedName("referedDate"    ) var referedDate    : String?  = null

)