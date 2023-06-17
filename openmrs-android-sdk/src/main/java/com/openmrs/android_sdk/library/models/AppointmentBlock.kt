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
import com.openmrs.android_sdk.library.databases.entities.LocationEntity
import java.io.Serializable

class AppointmentBlock(

    @Expose
    private val startDate: String? = null,

    @Expose
    private val endDate: String? = null,

    @Expose
    private val provider: Provider? = null,

    @Expose
    private val location: LocationEntity? = null,

    @Expose
    private val types: Set<AppointmentType>? = null

): Resource(), Serializable
