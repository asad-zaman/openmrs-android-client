package com.openmrs.android_sdk.library.models

import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.openmrs.android_sdk.library.models.typeConverters.PersonAttributeConverter

class PatientSaveDTO{

    @SerializedName("deathDate")
    @Expose
    var deathDate: String? = null

    @SerializedName("birthdate")
    @Expose
    var birthdate: String? = null

    @SerializedName("uuid")
    @Expose
    var uuid: String? = null

    @SerializedName("display")
    @Expose
    var display: String? = null

    @TypeConverters(PersonAttributeConverter::class)
    @SerializedName("attributes")
    @Expose
    var attributes: List<PersonAttribute> = ArrayList()

    @SerializedName("gender")
    @Expose
    var gender: String? = null

    @SerializedName("age")
    @Expose
    var age: Int? = null

    @SerializedName("birthtime")
    @Expose
    var birthtime: String? = null

    @SerializedName("deathdateEstimated")
    @Expose
    var deathdateEstimated: Boolean? = false

    @SerializedName("personUuid")
    @Expose
    var personUUID: String? = null

    @SerializedName("identifier")
    @Expose
    var identifier: String? = null

    @SerializedName("firstName")
    @Expose
    var firstName: String? = null

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null

    @SerializedName("birthPlace")
    @Expose
    var birthPlace: String? = null

    @SerializedName("mobile")
    @Expose
    var mobile: String? = null

    @SerializedName("nid")
    @Expose
    var nid: String? = null

    @SerializedName("motherName")
    @Expose
    var motherName: String? = null

    @SerializedName("motherNameBangla")
    @Expose
    var motherNameBangla: String? = null

    @SerializedName("fatherName")
    @Expose
    var fatherName: String? = null

    @SerializedName("fatherNameBangla")
    @Expose
    var fatherNameBangla: String? = null

    @SerializedName("nationality")
    @Expose
    var nationality: String? = null

    @SerializedName("occupation")
    @Expose
    var occupation: String? = null

    @SerializedName("relegion")
    @Expose
    var relegion: String? = null

    @SerializedName("division")
    @Expose
    var division: String? = null

    @SerializedName("bloodGroup")
    @Expose
    var bloodGroup: String? = null

    @SerializedName("eduQualification")
    @Expose
    var eduQualification: String? = null

    @SerializedName("matritalStatus")
    @Expose
    var matritalStatus: String? = null

    @SerializedName("ethnicity")
    @Expose
    var ethnicity: String? = null

    @SerializedName("fullNameBangla")
    @Expose
    var fullNameBangla: String? = null

    @SerializedName("disabilityType")
    @Expose
    var disabilityType: String? = null

    @SerializedName("spouseNameBangla")
    @Expose
    var spouseNameBangla: String? = null

    @SerializedName("spouseNameEnglish")
    @Expose
    var spouseNameEnglish: String? = null

    @SerializedName("district")
    @Expose
    var district: String? = null

    @SerializedName("upazila")
    @Expose
    var upazila: String? = null

    @SerializedName("paurasava")
    @Expose
    var paurasava: String? = null

    @SerializedName("unionName")
    @Expose
    var unionName: String? = null

    @SerializedName("ward")
    @Expose
    var ward: String? = null

    @SerializedName("patientAddress")
    @Expose
    var patientAddress: String? = null

    @SerializedName("unionId")
    @Expose
    var unionId: String? = "0"

    @SerializedName("divisionId")
    @Expose
    var divisionId: String? = null

    @SerializedName("districtId")
    @Expose
    var districtId: String? = null

    @SerializedName("upazilaId")
    @Expose
    var upazilaId: String? = null

    @SerializedName("paurasavaId")
    @Expose
    var paurasavaId: String? = null

    @SerializedName("wardId")
    @Expose
    var wardId: String? = null

    @SerializedName("location")
    @Expose
    var location: Int? = 0

    @SerializedName("countryId")
    @Expose
    var countryId: Int? = 0

    @SerializedName("blockId")
    @Expose
    var blockId: Int? = 0

}



data class TestResponse (
    val id: Any? = null,
    val person: Any? = null,
    val personUUID: String,
    val gender: String,
    val birthdate: Long,
    val firstName: String,
    val lastName: String,
    val identifier: String,
    val nid: String,
    val uuid: String,
    val brn: Any? = null,
    val epi: Any? = null,
    val mobile: Any? = null,
    val motherName: String,
    val shrID: Any? = null,
    val highRisk: Boolean,
    val fatherName: Any? = null,
    val spouseName: Any? = null,
    val refered: Boolean,
    val location: Long,
    val country: Any? = null,
    val division: String,
    val district: String,
    val upazila: String,
    val paurasava: String,
    val union: Any? = null,
    val ward: String,
    val block: Any? = null,
    val occupation: Any? = null,
    val relegion: Any? = null,
    val bloodGroup: Any? = null,
    val ethnicity: Any? = null,
    val nationality: Any? = null,
    val matritalStatus: Any? = null,
    val countryID: Long,
    val divisionID: Long,
    val districtID: Long,
    val upazilaID: Long,
    val paurasavaID: Long,
    val unionID: Long,
    val wardID: Long,
    val blockID: Long,
    val referedDate: Any? = null
)

