/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.mobile.activities.formdisplay

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.openmrs.android_sdk.library.models.Answer
import com.openmrs.android_sdk.library.models.Page
import com.openmrs.android_sdk.library.models.Question
import com.openmrs.android_sdk.library.models.Section
import com.openmrs.android_sdk.utilities.*
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.FORM_FIELDS_BUNDLE
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.FORM_PAGE_BUNDLE
import dagger.hilt.android.AndroidEntryPoint
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.BaseFragment
import org.openmrs.mobile.bundle.FormFieldsWrapper
import org.openmrs.mobile.databinding.FragmentFormDisplayBinding
import org.openmrs.mobile.utilities.ViewUtils.isEmpty
import java.util.*

@AndroidEntryPoint
class FormDisplayPageFragment : BaseFragment() {
    private var _binding: FragmentFormDisplayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FormDisplayPageViewModel by viewModels()

    private var formLabel: String = ""
    private var mSections: List<Section> = listOf()
    private lateinit var sectionContainer: LinearLayout
    private lateinit var sectionPrimaryContainer: LinearLayout
    private lateinit var sectionResultContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFormDisplayBinding.inflate(inflater, container, false)
        formLabel = viewModel.page.label ?: ""
        mSections = viewModel.page.sections
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        createFormViews()
        return binding.root
    }

    private fun createFormViews() = mSections.forEach { addSection(it) }

    private fun addSection(section: Section) {
        sectionPrimaryContainer = createSectionLayout(section.label!!)
        binding.sectionsPrimaryContainer.addView(sectionPrimaryContainer)
        initContainers().apply {
            if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.PREGNANCY_SERVICE){
                addQuestion(section.questions[0], sectionContainer)
            } else if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE){
                addQuestion(section.questions[0], sectionContainer)
                addQuestion(section.questions[3], sectionContainer)
                addQuestion(section.questions[5], sectionContainer)
            }
            //        section.questions.forEach { addQuestion(it, sectionContainer) }
        }
    }

    private fun initContainers() {
        sectionContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        binding.sectionsChildContainer.addView(sectionContainer)

        sectionResultContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        binding.sectionsResultContainer.addView(sectionResultContainer)
    }

    private fun createSectionLayout(sectionLabel: String): LinearLayout {
        val sectionContainer = LinearLayout(activity)
        val layoutParams = getAndAdjustLinearLayoutParams(sectionContainer)
        val labelTextView = TextView(activity).apply {
            text = sectionLabel
            gravity = Gravity.CENTER_HORIZONTAL
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(ContextCompat.getColor(requireActivity(), R.color.primary))
        }
        sectionContainer.addView(labelTextView, layoutParams)
        return sectionContainer
    }

    private fun addQuestion(question: Question, sectionContainer: LinearLayout) {
        when (question.questionOptions!!.rendering) {
            "group" -> {
                val questionGroupContainer: LinearLayout = createQuestionGroupLayout(question.label!!)
                sectionContainer.addView(questionGroupContainer)
                question.questions.forEach { subQuestion -> addQuestion(subQuestion, questionGroupContainer) }
            }
            "number" -> createAndAttachNumericQuestionEditText(question, sectionContainer)
            "text" -> createAndAttachNumericQuestionEditText(question, sectionContainer)
            "select" -> createAndAttachSelectQuestionDropdown(question, sectionContainer)
            "radio" -> createAndAttachSelectQuestionRadioButton(question, sectionContainer)
            "date" -> createDateView(question, sectionContainer)
            "repeating" -> createRepeatingView(question, sectionContainer)
        }
    }

    private fun createQuestionGroupLayout(questionLabel: String): LinearLayout {
        val questionGroupContainer = LinearLayout(activity)
        val layoutParams = getAndAdjustLinearLayoutParams(questionGroupContainer)
        val labelTextView = TextView(activity).apply {
            text = questionLabel
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(ContextCompat.getColor(requireActivity(), R.color.primary))
        }
        questionGroupContainer.addView(labelTextView, layoutParams)
        return questionGroupContainer
    }

    private fun createAndAttachNumericQuestionEditText(question: Question, sectionContainer: LinearLayout) {
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginInPxTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.toFloat(), resources.displayMetrics).toInt()
            val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
            setMargins(marginInPxLeft, marginInPxTop, 0, 0)
        }
        sectionContainer.addView(generateTextView(question.label))

        val inputField = viewModel.getOrCreateInputField(question.questionOptions!!.concept!!)

        val options = question.questionOptions!!
        if (options.min != null && options.max != null && !options.isAllowDecimal) {
            val dsb = DiscreteSeekBar(activity).apply {
                min = options.min!!.toInt()
                max = options.max!!.toInt()
                id = inputField.id
            }
            dsb.progress = inputField.numberValue.toInt()
            sectionContainer.addView(dsb, layoutParams)
            setOnProgressChangeListener(dsb, inputField)
        } else {
            val ed = RangeEditText(activity).apply {
                name = question.label
                hint = question.label
                isSingleLine = true
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                inputType = when (options.rendering) {
                    "number" -> { InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
                    "text" -> { InputType.TYPE_CLASS_TEXT }
                    else -> InputType.TYPE_CLASS_NUMBER
                }
                id = inputField.id
            }
            when {
                inputField.hasNumberValue -> {
                    ed.setText(inputField.numberValue.toString())
                    ed.setSelection(ed.length())
                }
                inputField.hasTextValue -> {
                    ed.setText(inputField.textValue)
                    ed.setSelection(ed.length())
                }
            }
            sectionContainer.addView(ed, layoutParams)
            setOnTextChangedListener(options.rendering!!, ed, inputField)
        }
    }

    private fun createAndAttachSelectQuestionDropdown(question: Question, mSectionContainer: LinearLayout) {
        val questionLinearLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val marginInPxTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.toFloat(), resources.displayMetrics).toInt()
                val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.toFloat(), resources.displayMetrics).toInt()
                setMargins(marginInPxLeft, marginInPxTop, 0, 0)
            }
        }

        // new added
        val mAnswers = mutableListOf(Answer("", getString(R.string.choose_one))).apply { addAll(question.questionOptions!!.answers!!) }

        val answerLabels = mAnswers.map { it.label }

        val spinner = layoutInflater.inflate(R.layout.form_dropdown, null) as Spinner

        spinner.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, answerLabels).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val spinnerField = SelectOneField(mAnswers, question.questionOptions!!.concept!!)

        questionLinearLayout.addView(generateTextView(question.label))
        questionLinearLayout.addView(spinner)

        when (question.label) {
            ApplicationConstants.FormListKeys.PREGNANCY_INFORMATION -> { sectionPrimaryContainer.addView(questionLinearLayout) }
            else -> { sectionContainer.addView(questionLinearLayout) }
        }

        val selectOneField = viewModel.findSelectOneFieldById(spinnerField.concept)
        if (selectOneField != null) {
            spinner.setSelection(selectOneField.chosenAnswerPosition)
            setOnItemSelectedListener(question.label ?: "", spinner, selectOneField)
        } else {
            viewModel.selectOneFields.add(spinnerField)
            setOnItemSelectedListener(question.label ?: "", spinner, spinnerField)
        }
    }

    private fun createRepeatingView(question: Question, sectionContainer: LinearLayout) {
        sectionResultContainer.addView(generateTextView(question.label))
        question.questions.forEach { addQuestion(it, sectionResultContainer) }
    }

    private fun createDateView(question: Question, sectionContainer: LinearLayout) {
        val dateText = TextView(activity).apply {
            setPadding(20, 0, 0, 0)
            text = "dd/mm/yyyy"
            val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(10, 0, marginInPx, 0)
            }
        }

        val questionLinearLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val marginInPxTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.toFloat(), resources.displayMetrics).toInt()
                val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.toFloat(), resources.displayMetrics).toInt()
                setMargins(marginInPxLeft, marginInPxTop, 0, 0)
            }
        }

        val dateTextLinearLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val inputField = viewModel.getOrCreateInputField(question.questionOptions!!.concept!!)

        val dateButton = Button(activity).apply {
            background = ContextCompat.getDrawable(context, R.drawable.ic_calender)
            val widthInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.toFloat(), resources.displayMetrics).toInt()
            val heightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.toFloat(), resources.displayMetrics).toInt()
            val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
            layoutParams = LinearLayout.LayoutParams(widthInPx, heightInPx).apply {
                setMargins(0, marginInPx, 0, 0)
            }
            setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = activity?.let { fa ->
                    DatePickerDialog(
                        fa,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                            dateText.text = selectedDate
                            inputField.textValue = selectedDate
                        },
                        year, month, day
                    )
                }
                datePickerDialog!!.show()
            }
        }

        dateTextLinearLayout.addView(dateText)
        dateTextLinearLayout.addView(dateButton)

        questionLinearLayout.addView(generateTextView(question.label))
        questionLinearLayout.addView(dateTextLinearLayout)

        sectionContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        sectionContainer.addView(questionLinearLayout)
    }

    private fun createAndAttachSelectQuestionRadioButton(question: Question, sectionContainer: LinearLayout) {
        val textView = TextView(activity).apply {
            setPadding(20, 0, 0, 0)
            text = question.label
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.DKGRAY)
        }
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginInPxTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.toFloat(), resources.displayMetrics).toInt()
            val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
            setMargins(marginInPxLeft, marginInPxTop, 0, 0)
        }
        val radioGroup = RadioGroup(activity)
        radioGroup.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
            setMargins(marginInPxLeft, 0, 0, 0)
        }
        question.questionOptions!!.answers!!.forEach {
            val radioButton = RadioButton(activity)
            radioButton.text = it.label
            radioGroup.addView(radioButton)
        }
        val radioGroupField = SelectOneField(question.questionOptions!!.answers!!, question.questionOptions!!.concept!!)
        val linearLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        sectionContainer.addView(textView)
        sectionContainer.addView(radioGroup)
        sectionContainer.layoutParams = linearLayoutParams

        val selectOneField = viewModel.findSelectOneFieldById(radioGroupField.concept!!)
        if (selectOneField != null) {
            if (selectOneField.chosenAnswerPosition != -1) {
                val radioButton = radioGroup.getChildAt(selectOneField.chosenAnswerPosition) as RadioButton
                radioButton.isChecked = true
            }
            setOnCheckedChangeListener(radioGroup, selectOneField)
        } else {
            setOnCheckedChangeListener(radioGroup, radioGroupField)
            viewModel.selectOneFields.add(radioGroupField)
        }
    }

    private fun setOnProgressChangeListener(dsb: DiscreteSeekBar, inputField: InputField) {
        dsb.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                inputField.numberValue = value.toDouble()
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
                // No override uses
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                // No override uses
            }

        })
    }

    private fun setOnTextChangedListener(renderType: String, et: EditText, inputField: InputField) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No override uses
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No override uses
            }

            override fun afterTextChanged(s: Editable?) {
                when(renderType){
                    "number" -> { inputField.numberValue = if (!s.isNullOrEmpty()) s.toString().toDouble() else InputField.DEFAULT_NUMBER_VALUE }
                    "text" -> { inputField.textValue = if (!s.isNullOrEmpty()) s.toString() else InputField.DEFAULT_TEXT_VALUE }
                }
            }

        })
    }

    private fun setOnItemSelectedListener(spinnerLabel: String, spinner: Spinner, spinnerField: SelectOneField) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                spinnerField.setAnswer(i)
                if(spinnerLabel == ApplicationConstants.FormListKeys.PREGNANCY_INFORMATION){
                    when (i) {
                        0 -> { sectionContainer.removeAllViews() }
                        1 -> {
                            sectionContainer.removeAllViews()
                            addQuestion(mSections[0].questions[1], sectionContainer)
                            addQuestion(mSections[0].questions[2], sectionContainer)
                            addQuestion(mSections[0].questions[3], sectionContainer)
                        }
                        2 -> {
                            sectionContainer.removeAllViews()
                            addQuestion(mSections[0].questions[4], sectionContainer)
                            addQuestion(mSections[0].questions[5], sectionContainer)
                            addQuestion(mSections[0].questions[6], sectionContainer)
                            addQuestion(mSections[0].questions[7], sectionContainer)
                            addQuestion(mSections[0].questions[8], sectionContainer)
                            addQuestion(mSections[0].questions[9], sectionContainer)
                            addQuestion(mSections[0].questions[10], sectionContainer)
                            addQuestion(mSections[0].questions[11], sectionContainer)
                            addQuestion(mSections[0].questions[12], sectionContainer)
                        }
                        3 -> {
                            sectionContainer.removeAllViews()
                            addQuestion(mSections[0].questions[16], sectionContainer)
                            addQuestion(mSections[0].questions[17], sectionContainer)
                        }
                        4 -> {
                            sectionContainer.removeAllViews()
                            addQuestion(mSections[0].questions[18], sectionContainer)
                        }
                        5 -> { sectionContainer.removeAllViews() }
                    }
                } else if (spinnerLabel == ApplicationConstants.FormListKeys.PREGNANCY_RESULT){
                    when (i) {
                        0 -> { sectionResultContainer.removeAllViews() }
                        1 -> {
                            sectionResultContainer.removeAllViews()
                            addQuestion(mSections[0].questions[13], sectionResultContainer)
                            addQuestion(mSections[0].questions[15], sectionResultContainer)
                        }
                        2 -> {
                            sectionResultContainer.removeAllViews()
                            addQuestion(mSections[0].questions[14], sectionResultContainer)
                        }
                    }
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                spinnerField.setAnswer(-1)
            }
        }
    }

    private fun setOnCheckedChangeListener(radioGroup: RadioGroup, radioGroupField: SelectOneField) {
        radioGroup.setOnCheckedChangeListener { radioGroup1: RadioGroup, i: Int ->
            val radioButton = radioGroup1.findViewById<View>(i)
            val idx = radioGroup1.indexOfChild(radioButton)
            radioGroupField.setAnswer(idx)
        }
    }

    private fun getAndAdjustLinearLayoutParams(linearLayout: LinearLayout): LinearLayout.LayoutParams {
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        linearLayout.orientation = LinearLayout.VERTICAL
        val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
        layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
        return layoutParams
    }

    private fun generateTextView(text: String?): View {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
                val marginInPxTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.toFloat(), resources.displayMetrics).toInt()
                val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
                setMargins(marginInPxLeft, marginInPxTop, 0, 0)
            }
        val textView = TextView(activity)
        textView.text = text
        textView.textSize = 17f
        textView.setTypeface(null, Typeface.BOLD)
        textView.setTextColor(Color.DKGRAY)
        textView.layoutParams = layoutParams
        return textView
    }

    fun checkInputFields(): Boolean {
        var allEmpty = true
        var valid = true
        for (field in viewModel.inputFields) {
            try {
                val ed: RangeEditText = requireActivity().findViewById(field.id)
                if (!isEmpty(ed)) {
                    allEmpty = false
                    if (!ed.validInput || ed.outOfRange) {
                        ed.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                        valid = false
                    }
                }
            } catch (e: ClassCastException) {
                val dsb: DiscreteSeekBar = requireActivity().findViewById(field.id)
                if (dsb.progress > dsb.min) allEmpty = false
            }
        }
        for (radioGroupField in viewModel.selectOneFields) {
            if (radioGroupField.chosenAnswer != null) allEmpty = false
        }

        if (allEmpty) ToastUtil.error(getString(R.string.all_fields_empty_error_message))
        else if (!valid) ToastUtil.error(getString(R.string.invalid_inputs))

        return !allEmpty && valid
    }

    fun validateInputFields(): Boolean {
        var isValid = true
        for (field in viewModel.inputFields) {
            if(field.numberValue == InputField.DEFAULT_NUMBER_VALUE && field.textValue == InputField.DEFAULT_TEXT_VALUE){
                isValid = false
                ToastUtil.error(getString(R.string.empty_field_error_message))
                break
            }
        }
        for (radioGroupField in viewModel.selectOneFields) {
            if (radioGroupField.chosenAnswer == null || radioGroupField.chosenAnswer!!.concept!!.isEmpty()){
                isValid = false
                ToastUtil.error(getString(R.string.empty_field_error_message))
                break
            }
        }

        return isValid
    }

    fun getInputFields() = viewModel.inputFields

    fun getSelectOneFields() = viewModel.selectOneFields

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(page: Page, formFieldsWrapper: FormFieldsWrapper?) = FormDisplayPageFragment().apply {
            arguments = bundleOf(
                    FORM_PAGE_BUNDLE to page,
                    FORM_FIELDS_BUNDLE to formFieldsWrapper
            )
        }
    }
}
