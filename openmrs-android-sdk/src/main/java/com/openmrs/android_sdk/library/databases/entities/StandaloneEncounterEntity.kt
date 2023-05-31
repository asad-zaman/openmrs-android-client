package com.openmrs.android_sdk.library.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.openmrs.android_sdk.library.models.Resource

/**
 * The type StandaloneEncounter entity.
 */
@Entity(tableName = "standaloneEncounters")
class StandaloneEncounterEntity : Resource {

    @ColumnInfo(name = "visit_uuid")
    var visitUuid: String? = null

    @ColumnInfo(name = "encounterDatetime")
    var encounterDateTime: String? = null
    /**
     * Gets and sets encounter type.
     *
     * @return the encounter type
     */
    @ColumnInfo(name = "type")

    var encounterType: String? = null
    /**
     * Gets and sets patient uuid.
     *
     * @return the patient uuid
     */
    @ColumnInfo(name = "patient_uuid")
    var patientUuid: String? = null
    /**
     * Gets and sets form uuid.
     *
     * @return the form uuid
     */
    @ColumnInfo(name = "form_uuid")

    var formUuid: String? = null
    /**
     * Gets and sets location uuid.
     *
     * @return the location uuid
     */
    @ColumnInfo(name = "location_uuid")
    var locationUuid: String? = null
    /**
     * Gets and sets encounter provider uuid.
     *
     * @return the encounter provider uuid
     */
    @ColumnInfo(name = "encounter_provider_uuid")
    var encounterProviderUuid: String? = null

    constructor(
        id: Long?,
        uuid: String?,
        display: String?,
        visitUuid: String?,
        encounterDateTime: String?,
        encounterType: String?,
        patientUuid: String?,
        formUuid: String?,
        locationUuid: String?,
        encounterProviderUuid: String?
    ) {
        this.id = id
        this.uuid = uuid
        this.display = display
        this.visitUuid = visitUuid
        this.encounterDateTime = encounterDateTime
        this.encounterType = encounterType
        this.patientUuid = patientUuid
        this.formUuid = formUuid
        this.locationUuid = locationUuid
        this.encounterProviderUuid = encounterProviderUuid
    }
}