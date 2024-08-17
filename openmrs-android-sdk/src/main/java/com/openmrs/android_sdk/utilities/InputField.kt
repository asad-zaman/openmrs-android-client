/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package com.openmrs.android_sdk.utilities

import java.io.Serializable
import kotlin.math.abs

data class InputField(var concept: String, var mRequired: Boolean) : Serializable {

    val id: Int = abs(concept.hashCode())

    var numberValue: Double = DEFAULT_NUMBER_VALUE
    val hasNumberValue: Boolean get() = numberValue != DEFAULT_NUMBER_VALUE

    var textValue: String = DEFAULT_TEXT_VALUE
    val hasTextValue: Boolean get() = textValue != DEFAULT_TEXT_VALUE

    companion object {
        const val DEFAULT_NUMBER_VALUE = -1.0
        const val DEFAULT_TEXT_VALUE = ""
    }
}
