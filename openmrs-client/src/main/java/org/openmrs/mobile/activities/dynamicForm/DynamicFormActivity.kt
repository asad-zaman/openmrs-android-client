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
package org.openmrs.mobile.activities.dynamicForm

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openmrs.android_sdk.library.models.Encounter
import com.openmrs.android_sdk.library.models.Observation
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.ToastUtil
import com.openmrs.android_sdk.utilities.ToastUtil.error
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.openmrs.mobile.activities.ACBaseActivity
import org.openmrs.mobile.databinding.DynamicFormViewBinding
import org.openmrs.mobile.listeners.ItemClickListener
import org.openmrs.mobile.models.NavDrawerItem
import java.io.InputStreamReader

@AndroidEntryPoint
class DynamicFormActivity : ACBaseActivity(), ItemClickListener {
    private lateinit var mBinding: DynamicFormViewBinding
    private var mItems: List<Observation> = listOf()
    private var mEncounter: Encounter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DynamicFormViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val encounterString = this.intent.getStringExtra(ApplicationConstants.BundleKeys.ENCOUNTER_ENTITY)
        mEncounter = Gson().fromJson(encounterString, Encounter::class.java)

        if(mEncounter != null){
            title = mEncounter!!.form?.display

            mBinding.rvDynamicView.layoutManager = LinearLayoutManager(this)
            mItems = mEncounter!!.observations

            val mAdapter = DynamicFormRecyclerAdapter(mItems)
            mBinding.rvDynamicView.adapter = mAdapter
        }

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.setFinishOnTouchOutside(false)

        mBinding.btnDynamicFormCancel.visibility = View.GONE
        mBinding.btnDynamicFormDone.setOnClickListener { finish() }
    }

    override fun onItemClicked(item: Any?) {}

    private fun loadJsonFromAssets(): String {
        var formName = ""
        /*when (formType) {
            ApplicationConstants.FormListKeys.PREGNANCY_SERVICE -> formName = "forms/pregnancy_service.json"
            ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE -> formName = "forms/pregnancy_service.json"
            ApplicationConstants.FormListKeys.GENERAL_PATIENT_SERVICE -> formName = "forms/pregnancy_service.json"
        }*/
        val inputStream = assets.open(formName)
        val reader = InputStreamReader(inputStream)
        return reader.readText().also { reader.close() }
        /*return assets.open("forms/pregnancy_service.json").use { inputStream ->
            InputStreamReader(inputStream).use { inputStreamReader ->
                inputStreamReader.readText()
            }
        }*/
    }

    private fun parseJson(jsonString: String): List<DynamicFormItem> {
        val type = object : TypeToken<List<DynamicFormItem>>() {}.type
        return Gson().fromJson(jsonString, type)
    }
}
