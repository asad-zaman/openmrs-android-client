/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.mobile.activities.formdisplay

//import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.openmrs.android_sdk.library.models.Answer
import com.openmrs.android_sdk.library.models.Page
import com.openmrs.android_sdk.library.models.Question
import com.openmrs.android_sdk.library.models.Section
import com.openmrs.android_sdk.utilities.*
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.FORM_FIELDS_BUNDLE
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.FORM_PAGE_BUNDLE
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.BaseFragment
import org.openmrs.mobile.bundle.FormFieldsWrapper
import org.openmrs.mobile.databinding.FragmentFormDisplayBinding
import org.openmrs.mobile.iot.IOTActivity
import org.openmrs.mobile.utilities.ViewUtils.isEmpty
import java.util.*


@AndroidEntryPoint
class FormDisplayPageFragment : BaseFragment() {
    private var _binding: FragmentFormDisplayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FormDisplayPageViewModel by viewModels()

    private var formLabel: String = ""
    private var mSections: List<Section> = listOf()
    private val containerList: MutableList<LinearLayout> = mutableListOf()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFormDisplayBinding.inflate(inflater, container, false)
        formLabel = viewModel.page.label ?: ""
        mSections = viewModel.page.sections
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        createFormViews()

        if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.PRE_PREGNANCY_SERVICE
            || formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.POST_PREGNANCY_SERVICE){
            val formFab = FloatingActionButton(requireContext()).apply {
                setImageResource(R.drawable.ic_bluetooth)
                size = FloatingActionButton.SIZE_NORMAL
                layoutParams = CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.END
                    marginEnd = 20
                    bottomMargin = 16
                }
                imageTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.white)
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)
            }
            (binding.root as? CoordinatorLayout)?.addView(formFab)

            activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
                StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val returnedValue = data.getStringExtra("resultKey")
                        ToastUtil.success("Received: $returnedValue")
                    }
                } else if (result.resultCode == Activity.RESULT_CANCELED) { }
            }

            formFab.setOnClickListener {
                val intent = Intent(requireActivity(), IOTActivity::class.java)
                activityResultLauncher.launch(intent)
            }
        }
        return binding.root
    }

    private fun createFormViews() = mSections.forEach { addSection(it) }

    private fun addSection(section: Section) {
        containerList.add(LinearLayout(activity))
        val lastIndex = containerList.size - 1
        containerList[lastIndex] = createSectionLayout(containerList[lastIndex], section.label!!)
        binding.sectionsPrimaryContainer.addView(containerList[lastIndex])
        if (formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.PREGNANCY_SERVICE) {
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[0], this) }
        } else if (formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE) {
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[0], this) }
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[3], this) }
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[5], this) }
        }  else if (formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.GENERAL_PATIENT_SERVICE) {
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[0], this) }
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[3], this) }
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[5], this) }
        } else if (formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.PRE_PREGNANCY_SERVICE) {
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[0], this) }
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[21], this) }
        } else if (formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.POST_PREGNANCY_SERVICE) {
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[0], this) }
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[15], this) }
            createContainer(prevContainer = containerList[0]).apply { addQuestion(section.questions[17], this) }
        }
    }

    private fun createContainer(dOrientation: Int = LinearLayout.VERTICAL, dHeight: Int = LinearLayout.LayoutParams.WRAP_CONTENT, dWidth: Int = LinearLayout.LayoutParams.MATCH_PARENT, prevContainer: LinearLayout) : LinearLayout {
        val mContainer = LinearLayout(activity).apply {
            orientation = dOrientation
            layoutParams = LinearLayout.LayoutParams(dWidth, dHeight)
        }
        prevContainer.addView(mContainer)
        return mContainer
    }

    private fun createSectionLayout(mContainer: LinearLayout, sectionLabel: String): LinearLayout {
        val layoutParams = getAndAdjustLinearLayoutParams(mContainer)
        val labelTextView = TextView(activity).apply {
            text = sectionLabel
            gravity = Gravity.CENTER_HORIZONTAL
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(ContextCompat.getColor(requireActivity(), R.color.primary))
        }
        mContainer.addView(labelTextView, layoutParams)
        return mContainer
    }

    private fun addQuestion(question: Question, mContainer: LinearLayout, createAsNew: Boolean = false) {
        when (question.questionOptions!!.rendering) {
            "group" -> {
                val questionGroupContainer: LinearLayout = createQuestionGroupLayout(question.label!!)
                mContainer.addView(questionGroupContainer)
                question.questions.forEach { subQuestion -> addQuestion(subQuestion, questionGroupContainer) }
            }
            "number", "text" -> createAndAttachNumericQuestionEditText(question, mContainer)
            "select" -> createAndAttachSelectQuestionDropdown(question, mContainer)
            "radio" -> createAndAttachSelectQuestionRadioButton(question, mContainer, createAsNew)
            "date" -> createDateView(question, mContainer)
            "repeating" -> createRepeatingView(question, mContainer)
            "multiCheckbox" -> createMultipleCheckboxView(question, mContainer)
        }
    }

    private fun createQuestionGroupLayout(questionLabel: String): LinearLayout {
        val questionGroupContainer = LinearLayout(activity)
        val layoutParams = getAndAdjustLinearLayoutParams(questionGroupContainer)
        val labelTextView = TextView(activity).apply {
            text = questionLabel
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(ContextCompat.getColor(requireActivity(), R.color.red))
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
        sectionContainer.addView(generateTextView(question))

        val inputField = viewModel.getOrCreateInputField(question.questionOptions!!.concept!!, question.required ?: true)

        val options = question.questionOptions!!
        if (options.min != null && options.max != null && !options.isAllowDecimal) {
            /*val dsb = DiscreteSeekBar(activity).apply {
                min = options.min!!.toInt()
                max = options.max!!.toInt()
                id = inputField.id
            }
            dsb.progress = inputField.numberValue.toInt()
            sectionContainer.addView(dsb, layoutParams)
            setOnProgressChangeListener(dsb, inputField)*/
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

    private fun createAndAttachSelectQuestionDropdown(question: Question, mContainer: LinearLayout) {
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

        val mTextview = generateTextView(question)

        // new added
        val mAnswers = mutableListOf(Answer("", getString(R.string.choose_one))).apply { addAll(question.questionOptions!!.answers!!) }

        val answerLabels = mAnswers.map { it.label }

        val spinner = layoutInflater.inflate(R.layout.form_dropdown, null) as Spinner

        spinner.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, answerLabels).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val spinnerField = SelectOneField(mAnswers, question.questionOptions!!.concept!!, question.required ?: true)

        questionLinearLayout.addView(mTextview)
        questionLinearLayout.addView(spinner)

        mContainer.addView(questionLinearLayout)

        val selectOneField = viewModel.findSelectOneFieldById(spinnerField.concept)
        if (selectOneField != null) {
            spinner.setSelection(selectOneField.chosenAnswerPosition)
            setOnItemSelectedListener(question.label ?: "", spinner, selectOneField)
        } else {
            viewModel.selectOneFields.add(spinnerField)
            setOnItemSelectedListener(question.label ?: "", spinner, spinnerField)
        }
    }

    private fun createRepeatingView(question: Question, mContainer: LinearLayout) {
        mContainer.addView(generateTextView(question))
        question.questions.forEach { addQuestion(it, mContainer) }
    }

    private fun createMultipleCheckboxView(question: Question, mContainer: LinearLayout) {
        mContainer.addView(generateTextView(question))

        val topLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val marginInPxTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
                val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
                setMargins(marginInPxLeft, marginInPxTop, 0, 0)
            }
        }

        val selectedCountTextView = TextView(activity).apply {
            text = "0"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
            )
        }

//        topLayout.addView(selectedCountTextView)

        val checkboxLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val clearButton = Button(activity).apply {
            val iconDrawable = ContextCompat.getDrawable(context,R.drawable.ic_close)
            iconDrawable?.let {
                it.colorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                setCompoundDrawablesWithIntrinsicBounds(resizeDrawable(it, dpToPx(25)), null, null, null)
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setOnClickListener { clearSelectedItems(checkboxLayout, selectedCountTextView) }
        }

//        topLayout.addView(clearButton)

        val searchEditText = EditText(activity).apply {
            hint = "খুঁজুন..."
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.9f
            )
        }

        topLayout.addView(searchEditText)

        mContainer.addView(topLayout)

        val multiCheckField = SelectOneField(question.questionOptions!!.answers!!, question.questionOptions!!.concept!!, question.required ?: true)
        val selectOneField = viewModel.findSelectOneFieldById(multiCheckField.concept)

        if (selectOneField != null) {
            setOnMultiCheckItemSelectListener(question.label ?: "", question, selectOneField, mContainer, checkboxLayout)
        } else {
            viewModel.selectOneFields.add(multiCheckField)
            setOnMultiCheckItemSelectListener(question.label ?: "", question, multiCheckField, mContainer, checkboxLayout)
        }

        /*container.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (checkboxLayout.visibility == View.VISIBLE) {
                    checkboxLayout.visibility = View.GONE
                }
            }
            false
        }

        searchEditText.setOnClickListener {
            checkboxLayout.visibility = View.VISIBLE
        }*/

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().toLowerCase()
                checkboxLayout.children.forEach { view ->
                    if (view is CheckBox) {
                        view.visibility = if (view.text.toString().toLowerCase().contains(query)) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun resizeDrawable(drawable: Drawable, sizePx: Int): Drawable {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, false)
        return BitmapDrawable(resources, resizedBitmap)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }

    private fun clearSelectedItems(checkboxLayout: LinearLayout, selectedCountTextView: TextView) {
        selectedCountTextView.text = "0"
        checkboxLayout.children.forEach { view ->
            if (view is CheckBox) {
                view.isChecked = false
            }
        }
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

        val inputField = viewModel.getOrCreateInputField(question.questionOptions!!.concept!!, question.required ?: true)

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
                            dateText.text = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                            inputField.textValue = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                        },
                        year, month, day
                    )
                }
                datePickerDialog!!.show()
            }
        }

        dateTextLinearLayout.addView(dateText)
        dateTextLinearLayout.addView(dateButton)

        questionLinearLayout.addView(generateTextView(question))
        questionLinearLayout.addView(dateTextLinearLayout)

        sectionContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        sectionContainer.addView(questionLinearLayout)
    }

    private fun createAndAttachSelectQuestionRadioButton(question: Question, sectionContainer: LinearLayout, createAsNew: Boolean = false) {
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
        val radioGroupField = SelectOneField(question.questionOptions!!.answers!!, question.questionOptions!!.concept!!, question.required ?: true)

        sectionContainer.addView(generateTextView(question))
        sectionContainer.addView(radioGroup)

        if(createAsNew){
            sectionContainer.addView(LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.toFloat(), resources.displayMetrics).toInt()
                    setMargins(marginInPxLeft, 0, 0, 0)
                }
            })
        }

        val selectOneField = viewModel.findSelectOneFieldById(radioGroupField.concept)
        if (selectOneField != null) {
            if (selectOneField.chosenAnswerPosition != -1) {
                val radioButton = radioGroup.getChildAt(selectOneField.chosenAnswerPosition) as RadioButton
                radioButton.isChecked = true
            }
            setOnCheckedChangeListener(question.label ?: "", radioGroup, selectOneField)
        } else {
            setOnCheckedChangeListener(question.label ?: "", radioGroup, radioGroupField)
            viewModel.selectOneFields.add(radioGroupField)
        }
    }

    /*private fun setOnProgressChangeListener(dsb: DiscreteSeekBar, inputField: InputField) {
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
    }*/

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

    private fun setOnItemSelectedListener(mLabel: String, spinner: Spinner, spinnerField: SelectOneField) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                spinnerField.setAnswer(i)
                when (mLabel) {
                    ApplicationConstants.FormQuestionKeys.PREGNANCY_INFORMATION -> { handlePregnancyInformationSpinner(i) }
                    ApplicationConstants.FormQuestionKeys.PREGNANCY_RESULT -> { handlePregnancyResultSpinner(i) }
                    ApplicationConstants.FormQuestionKeys.DONE_HEALTH_SERVICE -> { handleHealthServiceDone(i) }
                    ApplicationConstants.FormQuestionKeys.DONE_HEALTH_SERVICE_WITH_SPACE -> { handleHealthServiceWithSpaceDone(i) }
                    ApplicationConstants.FormQuestionKeys.DONE_HEALTH_EDUCATION -> { handleHealthEducationDone(i) }
                    ApplicationConstants.FormQuestionKeys.DONE_REFER -> { handleReferDone(i) }
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                spinnerField.setAnswer(-1)
            }
        }
    }

    private fun setOnMultiCheckItemSelectListener(mLabel: String, mQuestion: Question, selectOneField: SelectOneField, mContainer: LinearLayout, checkboxLayout: LinearLayout) {
        val selectedAnswers = mutableListOf<Answer>()
        mQuestion.questionOptions?.answers?.forEach { answer ->
            val checkBox = CheckBox(activity).apply {
                text = answer.label
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedAnswers.add(answer)
                    } else {
                        selectedAnswers.remove(answer)
                    }
                    selectOneField.chosenAnswers = selectedAnswers
//                    selectedCountTextView.text = "${selectedOptions.size}"
                }
            }
            checkboxLayout.addView(checkBox)
        }

        mContainer.addView(checkboxLayout)
    }

    private fun handlePregnancyInformationSpinner(idx: Int) {
        val lastIndex = 1
        val mParent = containerList[0][lastIndex] as LinearLayout
        val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)
        viewModel.inputFields = mutableListOf()
        viewModel.selectOneFields.retainAll { it == viewModel.selectOneFields.first() }
        when (idx) {
            0, 5 -> {}
            1 -> {
                addQuestion(mSections[0].questions[1], childLayout)
                addQuestion(mSections[0].questions[2], childLayout)
                addQuestion(mSections[0].questions[3], childLayout)
            }
            2 -> {
                addQuestion(mSections[0].questions[4], childLayout)
                addQuestion(mSections[0].questions[5], childLayout)
                addQuestion(mSections[0].questions[6], childLayout)
                addQuestion(mSections[0].questions[7], childLayout)
                addQuestion(mSections[0].questions[8], childLayout)
                addQuestion(mSections[0].questions[9], childLayout)
                addQuestion(mSections[0].questions[10], childLayout)
                addQuestion(mSections[0].questions[11], childLayout)
                createContainer(prevContainer = childLayout).apply {
                    addQuestion(mSections[0].questions[12], this)
                }
            }
            3 -> {
                addQuestion(mSections[0].questions[16], childLayout)
                addQuestion(mSections[0].questions[17], childLayout)
            }
            4 -> {
                addQuestion(mSections[0].questions[18], childLayout)
            }
        }
    }

    private fun handlePregnancyResultSpinner(idx: Int) {
        val lastIndex = 1
        val mParent = containerList[0][lastIndex] as LinearLayout
        val cLayout1 = mParent.getChildAt(mParent.childCount - 1) as LinearLayout
        val cLayout2= cLayout1.getChildAt(cLayout1.childCount - 1) as LinearLayout
        val childLayout = cLayout2.getChildAt(cLayout2.childCount - 1) as LinearLayout
        val finalLayout = getChildLayout(childLayout.getChildAt(childLayout.childCount - 1) as LinearLayout)
        viewModel.inputFields.retainAll(viewModel.inputFields.take(2))
        viewModel.selectOneFields.retainAll(viewModel.selectOneFields.take(8))
        when (idx) {
            0 -> {}
            1 -> {
                addQuestion(mSections[0].questions[13], finalLayout)
                addQuestion(mSections[0].questions[15], finalLayout)
            }
            2 -> {
                addQuestion(mSections[0].questions[14], finalLayout)
            }
        }
    }

    private fun handleHealthServiceDone(idx: Int) {
        if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE){
            val lastIndex = 1
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            viewModel.inputFields = mutableListOf()
            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[1].questionOptions?.concept) { iterator.remove() }
            }
            when (idx) {
                0, 2 -> {}
                1 -> {
                    addQuestion(mSections[0].questions[1], childLayout)
                    addQuestion(mSections[0].questions[2], childLayout)
                }
            }
        } else if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.POST_PREGNANCY_SERVICE) {
            val lastIndex = 1
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            val mIterator = viewModel.inputFields.iterator()
            while (mIterator.hasNext()) {
                val it = mIterator.next()
                if (it.concept == mSections[0].questions[3].questionOptions?.concept
                    || it.concept == mSections[0].questions[6].questionOptions?.concept
                    || it.concept == mSections[0].questions[7].questionOptions?.concept) {
                    mIterator.remove()
                }
            }

            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[1].questionOptions?.concept
                    || it.concept == mSections[0].questions[2].questionOptions?.concept
                    || it.concept == mSections[0].questions[4].questionOptions?.concept
                    || it.concept == mSections[0].questions[5].questionOptions?.concept
                    || it.concept == mSections[0].questions[8].questionOptions?.concept
                    || it.concept == mSections[0].questions[9].questionOptions?.concept
                    || it.concept == mSections[0].questions[10].questionOptions?.concept
                    || it.concept == mSections[0].questions[11].questionOptions?.concept
                    || it.concept == mSections[0].questions[12].questionOptions?.concept
                    || it.concept == mSections[0].questions[13].questionOptions?.concept
                    || it.concept == mSections[0].questions[14].questionOptions?.concept) {
                    iterator.remove()
                }
            }
            when (idx) {
                0, 2 -> {}
                1 -> {
                    addQuestion(mSections[0].questions[1], childLayout)
                    addQuestion(mSections[0].questions[2], childLayout)
                    createContainer(prevContainer = childLayout).apply {
                        addQuestion(mSections[0].questions[3], this)
                    }
                    addQuestion(mSections[0].questions[4], childLayout)
                    addQuestion(mSections[0].questions[5], childLayout)
                    createContainer(prevContainer = childLayout).apply {
                        addQuestion(mSections[0].questions[6], this)
                        addQuestion(mSections[0].questions[7], this)
                    }
                    addQuestion(mSections[0].questions[8], childLayout)
                    addQuestion(mSections[0].questions[9], childLayout)
                    addQuestion(mSections[0].questions[10], childLayout)
                    addQuestion(mSections[0].questions[11], childLayout)
                    addQuestion(mSections[0].questions[12], childLayout, true)
                    addQuestion(mSections[0].questions[14], childLayout)
                }
            }
        }
    }

    private fun handleHealthServiceWithSpaceDone(idx: Int) {
        if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.PRE_PREGNANCY_SERVICE) {
            val lastIndex = 1
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            val mIterator = viewModel.inputFields.iterator()
            while (mIterator.hasNext()) {
                val it = mIterator.next()
                if (it.concept == mSections[0].questions[1].questionOptions?.concept
                    || it.concept == mSections[0].questions[3].questionOptions?.concept
                    || it.concept == mSections[0].questions[6].questionOptions?.concept
                    || it.concept == mSections[0].questions[7].questionOptions?.concept) {
                    mIterator.remove()
                }
            }

            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[2].questionOptions?.concept
                    || it.concept == mSections[0].questions[4].questionOptions?.concept
                    || it.concept == mSections[0].questions[5].questionOptions?.concept
                    || it.concept == mSections[0].questions[8].questionOptions?.concept
                    || it.concept == mSections[0].questions[9].questionOptions?.concept
                    || it.concept == mSections[0].questions[10].questionOptions?.concept
                    || it.concept == mSections[0].questions[11].questionOptions?.concept
                    || it.concept == mSections[0].questions[12].questionOptions?.concept
                    || it.concept == mSections[0].questions[13].questionOptions?.concept
                    || it.concept == mSections[0].questions[14].questionOptions?.concept
                    || it.concept == mSections[0].questions[15].questionOptions?.concept
                    || it.concept == mSections[0].questions[16].questionOptions?.concept
                    || it.concept == mSections[0].questions[17].questionOptions?.concept
                    || it.concept == mSections[0].questions[18].questionOptions?.concept
                    || it.concept == mSections[0].questions[19].questionOptions?.concept
                    || it.concept == mSections[0].questions[20].questionOptions?.concept) {
                    iterator.remove()
                }
            }
            when (idx) {
                0, 2 -> {}
                1 -> {
                    createContainer(prevContainer = childLayout).apply {
                        addQuestion(mSections[0].questions[1], this)
                    }
                    addQuestion(mSections[0].questions[2], childLayout)
                    createContainer(prevContainer = childLayout).apply {
                        addQuestion(mSections[0].questions[3], this)
                    }
                    addQuestion(mSections[0].questions[4], childLayout)
                    addQuestion(mSections[0].questions[5], childLayout)
                    createContainer(prevContainer = childLayout).apply {
                        addQuestion(mSections[0].questions[6], this)
                        addQuestion(mSections[0].questions[7], this)
                    }
                    addQuestion(mSections[0].questions[8], childLayout)
                    addQuestion(mSections[0].questions[9], childLayout)
                    addQuestion(mSections[0].questions[10], childLayout)
                    addQuestion(mSections[0].questions[11], childLayout)
                    addQuestion(mSections[0].questions[12], childLayout)
                    addQuestion(mSections[0].questions[13], childLayout)
                    addQuestion(mSections[0].questions[14], childLayout)
                    addQuestion(mSections[0].questions[15], childLayout, true)
                    addQuestion(mSections[0].questions[17], childLayout)
                    addQuestion(mSections[0].questions[18], childLayout)
                    addQuestion(mSections[0].questions[19], childLayout)
                    addQuestion(mSections[0].questions[20], childLayout)
                }
            }
        }
    }

    private fun handleHealthEducationDone(idx: Int) {
        if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE){
            val lastIndex = 2
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[4].questionOptions?.concept) { iterator.remove() }
            }
            when (idx) {
                0, 2 -> {}
                1 -> {
                    addQuestion(mSections[0].questions[4], childLayout)
                }
            }
        } else if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.PRE_PREGNANCY_SERVICE){
            val lastIndex = 2
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[22].questionOptions?.concept) { iterator.remove() }
            }
            when (idx) {
                0, 2 -> {}
                1 -> { addQuestion(mSections[0].questions[22], childLayout) }
            }
        } else if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.POST_PREGNANCY_SERVICE){
            val lastIndex = 2
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[16].questionOptions?.concept) {
                    iterator.remove()
                }
            }
            when (idx) {
                0, 2 -> {}
                1 -> {
                    addQuestion(mSections[0].questions[16], childLayout)
                }
            }
        }
    }

    private fun handleReferDone(idx: Int) {
        if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.FAMILY_PLANNING_SERVICE){
            val lastIndex = 3
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[6].questionOptions?.concept || it.concept == mSections[0].questions[7].questionOptions?.concept) {
                    iterator.remove()
                }
            }
            when (idx) {
                0, 2 -> {}
                1 -> {
                    addQuestion(mSections[0].questions[6], childLayout)
                    addQuestion(mSections[0].questions[7], childLayout)
                }
            }
        } else if(formLabel.isNotEmpty() && formLabel == ApplicationConstants.FormListKeys.POST_PREGNANCY_SERVICE){
            val lastIndex = 3
            val mParent = containerList[0][lastIndex] as LinearLayout
            val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout)

            val iterator = viewModel.selectOneFields.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (it.concept == mSections[0].questions[18].questionOptions?.concept || it.concept == mSections[0].questions[19].questionOptions?.concept) {
                    iterator.remove()
                }
            }
            when (idx) {
                0, 2 -> {}
                1 -> {
                    addQuestion(mSections[0].questions[18], childLayout)
                    addQuestion(mSections[0].questions[19], childLayout)
                }
            }
        }
    }

    private fun getChildLayout(parentLayout: LinearLayout, keepChildViews: Boolean = false): LinearLayout {
        val childLayout: LinearLayout
        val lastChild = parentLayout.getChildAt(parentLayout.childCount - 1)
        if (lastChild.javaClass.simpleName == "LinearLayout") {
            childLayout = lastChild as LinearLayout
            if(!keepChildViews) childLayout.removeAllViews()
        } else {
            childLayout = createContainer(LinearLayout.VERTICAL, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, parentLayout)
        }
        return childLayout
    }

    private fun setOnCheckedChangeListener(mLabel: String, radioGroup: RadioGroup, radioGroupField: SelectOneField) {
        radioGroup.setOnCheckedChangeListener { radioGroup1: RadioGroup, i: Int ->
            val radioButton = radioGroup1.findViewById<View>(i)
            val idx = radioGroup1.indexOfChild(radioButton)
            radioGroupField.setAnswer(idx)
            when (mLabel) {
                ApplicationConstants.FormQuestionKeys.DONE_HEALTH_SERVICE -> {
                    val lastIndex = 1
                    val childLayout = getChildLayout(containerList[0][lastIndex] as LinearLayout, false)

                    val iterator = viewModel.selectOneFields.iterator()
                    while (iterator.hasNext()) {
                        val it = iterator.next()
                        if (it.concept == mSections[0].questions[1].questionOptions?.concept || it.concept == mSections[0].questions[2].questionOptions?.concept) {
                            iterator.remove()
                        }
                    }
                    when (idx) {
                        0 -> {
                            addQuestion(mSections[0].questions[1], childLayout)
                            addQuestion(mSections[0].questions[2], childLayout)
                        }
                        1 -> {}
                    }
                }
                ApplicationConstants.FormQuestionKeys.DONE_HEALTH_EDUCATION_NO_SPACE -> {
                    val lastIndex = 2
                    val childLayout = getChildLayout(containerList[0][lastIndex] as LinearLayout)
                    val iterator = viewModel.selectOneFields.iterator()
                    while (iterator.hasNext()) {
                        val it = iterator.next()
                        if (it.concept == mSections[0].questions[4].questionOptions?.concept) { iterator.remove() }
                    }
                    when (idx) {
                        0 -> {addQuestion(mSections[0].questions[4], childLayout)}
                        1 -> {}
                    }
                }
                ApplicationConstants.FormQuestionKeys.DONE_REFER -> {
                    val lastIndex = 3
                    val childLayout = getChildLayout(containerList[0][lastIndex] as LinearLayout, true)
                    val iterator = viewModel.selectOneFields.iterator()
                    while (iterator.hasNext()) {
                        val it = iterator.next()
                        if (it.concept == mSections[0].questions[6].questionOptions?.concept || it.concept == mSections[0].questions[7].questionOptions?.concept) {
                            iterator.remove()
                        }
                    }
                    when (idx) {
                        0 -> {
                            addQuestion(mSections[0].questions[6], childLayout)
                            addQuestion(mSections[0].questions[7], childLayout)
                        }
                        1 -> {}
                    }
                }
                ApplicationConstants.FormQuestionKeys.PREGNANCY_DANGER -> {
                    val lastIndex = 1
                    val mParent = containerList[0][lastIndex] as LinearLayout
                    val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout, true)
                    childLayout.children.forEachIndexed { mIndex, mView ->
                        if(mView.javaClass.simpleName == "TextView"){
                            val tValue = if((mView as TextView).text is String){ mView.text as String } else { (mView.text as Spanned).toString() }
                            if(tValue == ApplicationConstants.FormQuestionKeys.PREGNANCY_DANGER || tValue == "${ApplicationConstants.FormQuestionKeys.PREGNANCY_DANGER} *"){
                                val finalLayout = childLayout.getChildAt(mIndex + 2) as LinearLayout
                                val iterator = viewModel.selectOneFields.iterator()
                                while (iterator.hasNext()) {
                                    val it = iterator.next()
                                    if (it.concept == mSections[0].questions[16].questionOptions?.concept) { iterator.remove() }
                                }
                                if(idx == 0) { addQuestion(mSections[0].questions[16], finalLayout) } else finalLayout.removeAllViews()
                                return@forEachIndexed
                            }
                        }
                    }
                }
                ApplicationConstants.FormQuestionKeys.HEIGHT_NOT_MEASURABLE -> {
                    val lastIndex = 1
                    val mParent = containerList[0][lastIndex] as LinearLayout
                    val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout, true)
                    childLayout.children.forEachIndexed { mIndex, mView ->
                        if(mView.javaClass.simpleName == "TextView"){
                            val tValue = if((mView as TextView).text is String){ mView.text as String } else { (mView.text as Spanned).toString() }
                            if(tValue == ApplicationConstants.FormQuestionKeys.HEIGHT_NOT_MEASURABLE || tValue == "${ApplicationConstants.FormQuestionKeys.HEIGHT_NOT_MEASURABLE} *"){
                                (childLayout.getChildAt(mIndex - 1) as LinearLayout).removeAllViews()
                                return@forEachIndexed
                            }
                        }
                    }
                }
                ApplicationConstants.FormQuestionKeys.WEIGHT_NOT_MEASURABLE,
                ApplicationConstants.FormQuestionKeys.NOT_MEASURABLE -> {
                    val lastIndex = 1
                    val mParent = containerList[0][lastIndex] as LinearLayout
                    val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout, true)
                    childLayout.children.forEachIndexed { mIndex, mView ->
                        if(mView.javaClass.simpleName == "TextView"){
                            val tValue = if((mView as TextView).text is String){ mView.text as String } else { (mView.text as Spanned).toString() }
                            if(tValue == ApplicationConstants.FormQuestionKeys.WEIGHT_NOT_MEASURABLE || tValue == ApplicationConstants.FormQuestionKeys.NOT_MEASURABLE || tValue == "${ApplicationConstants.FormQuestionKeys.WEIGHT_NOT_MEASURABLE} *" || tValue == "${ApplicationConstants.FormQuestionKeys.NOT_MEASURABLE} *"){
                                (childLayout.getChildAt(mIndex - 1) as LinearLayout).removeAllViews()
                                return@forEachIndexed
                            }
                        }
                    }
                }
                ApplicationConstants.FormQuestionKeys.BLOOD_PRESSURE_SITUATION_NOT_MEASURABLE,
                ApplicationConstants.FormQuestionKeys.BLOOD_PRESSURE_NOT_MEASURABLE  -> {
                    val lastIndex = 1
                    val mParent = containerList[0][lastIndex] as LinearLayout
                    val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout, true)
                    childLayout.children.forEachIndexed { mIndex, mView ->
                        if(mView.javaClass.simpleName == "TextView"){
                            val tValue = if((mView as TextView).text is String){ mView.text as String } else { (mView.text as Spanned).toString() }
                            if(tValue == ApplicationConstants.FormQuestionKeys.BLOOD_PRESSURE_SITUATION_NOT_MEASURABLE || tValue == ApplicationConstants.FormQuestionKeys.BLOOD_PRESSURE_NOT_MEASURABLE || tValue == "${ApplicationConstants.FormQuestionKeys.BLOOD_PRESSURE_SITUATION_NOT_MEASURABLE} *" || tValue == "${ApplicationConstants.FormQuestionKeys.BLOOD_PRESSURE_NOT_MEASURABLE} *"){
                                (childLayout.getChildAt(mIndex - 1) as LinearLayout).removeAllViews()
                                return@forEachIndexed
                            }
                        }
                    }
                }
                ApplicationConstants.FormQuestionKeys.POST_PREGNANCY_DANGER -> {
                    val lastIndex = 1
                    val mParent = containerList[0][lastIndex] as LinearLayout
                    val childLayout = getChildLayout(mParent.getChildAt(mParent.childCount - 1) as LinearLayout, true)
                    childLayout.children.forEachIndexed { mIndex, mView ->
                        if(mView.javaClass.simpleName == "TextView"){
                            val tValue = if((mView as TextView).text is String){ mView.text as String } else { (mView.text as Spanned).toString() }
                            if(tValue == ApplicationConstants.FormQuestionKeys.POST_PREGNANCY_DANGER || tValue == "${ApplicationConstants.FormQuestionKeys.POST_PREGNANCY_DANGER} *"){
                                val finalLayout = childLayout.getChildAt(mIndex + 2) as LinearLayout
                                val iterator = viewModel.selectOneFields.iterator()
                                while (iterator.hasNext()) {
                                    val it = iterator.next()
                                    if (it.concept == mSections[0].questions[13].questionOptions?.concept) { iterator.remove() }
                                }
                                if(idx == 0){ addQuestion(mSections[0].questions[13], finalLayout) } else finalLayout.removeAllViews()
                                return@forEachIndexed
                            }
                        }
                    }
                }
            }
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

    private fun generateTextView(question: Question): View {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
                val marginInPxTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20.toFloat(), resources.displayMetrics).toInt()
                val marginInPxLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat(), resources.displayMetrics).toInt()
                setMargins(marginInPxLeft, marginInPxTop, 0, 0)
            }
        val textView = TextView(activity)
        val mRequired : Boolean= if(question.required != null) question.required!! else true
        if(mRequired) {
            val finalText = "${question.label} *"
            val ss = SpannableString(finalText).apply {
                setSpan(StyleSpan(Typeface.BOLD), finalText.length - 1, finalText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(Color.RED), finalText.length - 1, finalText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = ss
        } else {
            textView.text = question.label!!
        }
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
                /*val dsb: DiscreteSeekBar = requireActivity().findViewById(field.id)
                if (dsb.progress > dsb.min) allEmpty = false*/
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
            if(field.mRequired && field.numberValue == InputField.DEFAULT_NUMBER_VALUE && field.textValue == InputField.DEFAULT_TEXT_VALUE){
                isValid = false
                break
            }
        }
        for (radioGroupField in viewModel.selectOneFields) {
            if(radioGroupField.mRequired){
                if (radioGroupField.chosenAnswer == null || radioGroupField.chosenAnswer!!.concept!!.isEmpty()){
                    if(radioGroupField.chosenAnswers.isEmpty()){
                        isValid = false
                        break
                    }
                }
            }
        }
        if(!isValid) ToastUtil.error(getString(R.string.empty_field_error_message))
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
