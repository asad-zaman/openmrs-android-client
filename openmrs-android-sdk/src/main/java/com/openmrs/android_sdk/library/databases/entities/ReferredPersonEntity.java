package com.openmrs.android_sdk.library.databases.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.openmrs.android_sdk.library.models.Resource;

/**
 * The type Patient entity.
 */
@Entity(tableName = "referredPersons")
public class ReferredPersonEntity extends Resource {
    @ColumnInfo(name = "identifier")
    private String identifier;
    @ColumnInfo(name = "personUuid")
    private String personUuid;
    @ColumnInfo(name = "firstName")
    private String firstName;
    @ColumnInfo(name = "middleName")
    private String middleName;
    @ColumnInfo(name = "lastName")
    private String lastName;
    @ColumnInfo(name = "gender")
    private String gender;
    @ColumnInfo(name = "birthdate")
    private String birthDate;
    @ColumnInfo(name = "age")
    private String age;
    @ColumnInfo(name = "nid")
    private String nid;
    @ColumnInfo(name = "encounters")
    private String encounters;
    @ColumnInfo(name = "person")
    private Integer person;
    @ColumnInfo(name = "brn")
    private String brn;
    @ColumnInfo(name = "epi")
    private String epi;
    @ColumnInfo(name = "mobile")
    private String mobile;
    @ColumnInfo(name = "motherName")
    private String motherName;
    @ColumnInfo(name = "shrId")
    private String shrId;
    @ColumnInfo(name = "highRisk")
    private String highRisk;
    @ColumnInfo(name = "fatherName")
    private String fatherName;
    @ColumnInfo(name = "spouseName")
    private String spouseName;
    @ColumnInfo(name = "refered")
    private boolean refered;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "division")
    private String division;
    @ColumnInfo(name = "district")
    private String district;
    @ColumnInfo(name = "upazila")
    private String upazila;
    @ColumnInfo(name = "paurasava")
    private String paurasava;
    @ColumnInfo(name = "union")
    private String union;
    @ColumnInfo(name = "ward")
    private String ward;
    @ColumnInfo(name = "block")
    private String block;
    @ColumnInfo(name = "occupation")
    private String occupation;
    @ColumnInfo(name = "relegion")
    private String relegion;
    @ColumnInfo(name = "bloodGroup")
    private String bloodGroup;
    @ColumnInfo(name = "ethnicity")
    private String ethnicity;
    @ColumnInfo(name = "nationality")
    private String nationality;
    @ColumnInfo(name = "matritalStatus")
    private String matritalStatus;
    @ColumnInfo(name = "countryId")
    private String countryId;
    @ColumnInfo(name = "divisionId")
    private String divisionId;
    @ColumnInfo(name = "districtId")
    private String districtId;
    @ColumnInfo(name = "upazilaId")
    private String upazilaId;
    @ColumnInfo(name = "paurasavaId")
    private String paurasavaId;
    @ColumnInfo(name = "unionId")
    private String unionId;
    @ColumnInfo(name = "wardId")
    private String wardId;
    @ColumnInfo(name = "blockId")
    private String blockId;
    @ColumnInfo(name = "referedDate")
    private String referedDate;

    public ReferredPersonEntity() {
    }
}
