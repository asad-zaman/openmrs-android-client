package org.openmrs.mobile.activities.dynamicForm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginBottom
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.openmrs.android_sdk.utilities.ApplicationConstants
import org.openmrs.mobile.R
import org.openmrs.mobile.listeners.ItemClickListener

class DynamicFormRecyclerAdapter(private val widgets: List<DynamicFormItem>,
                                 private val itemChangeListener: ItemClickListener
     ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXTVIEW = 0
        private const val TYPE_EDITTEXT = 1
        private const val TYPE_DROPDOWN = 2
        private const val TYPE_RADIOBUTTON = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (widgets[position].type) {
            ApplicationConstants.WidgetTypes.TEXTVIEW -> TYPE_TEXTVIEW
            ApplicationConstants.WidgetTypes.EDITTEXT -> TYPE_EDITTEXT
            ApplicationConstants.WidgetTypes.DROPDOWN -> TYPE_DROPDOWN
            ApplicationConstants.WidgetTypes.RADIOBUTTON -> TYPE_RADIOBUTTON
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXTVIEW -> {
                val view = inflater.inflate(R.layout.dynamic_textview, parent, false)
                TextViewHolder(view)
            }
            TYPE_EDITTEXT -> {
                val view = inflater.inflate(R.layout.dynamic_edittext, parent, false)
                EditTextViewHolder(view)
            }
            TYPE_DROPDOWN -> {
                val view = inflater.inflate(R.layout.dynamic_dropdown, parent, false)
                DropdownViewHolder(view)
            }
            TYPE_RADIOBUTTON -> {
                val view = inflater.inflate(R.layout.dynamic_radiobutton, parent, false)
                RadioButtonViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextViewHolder -> holder.bind(widgets[position])
            is EditTextViewHolder -> holder.bind(widgets[position])
            is DropdownViewHolder -> holder.bind(widgets[position])
            is RadioButtonViewHolder -> holder.bind(widgets[position])
        }
    }

    override fun getItemCount(): Int = widgets.size

    inner class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.dynamicTV)
        fun bind(item: DynamicFormItem) {
            textView.text = item.value
        }
    }

    inner class EditTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val editText: EditText = view.findViewById(R.id.dynamicET)
        fun bind(item: DynamicFormItem) {
            editText.hint = item.hint
            editText.addTextChangedListener {
                item.value = it.toString()
                itemChangeListener.onItemClicked(item)
            }
        }
    }

    inner class DropdownViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val spinner: Spinner = view.findViewById(R.id.dynamicDD)
        fun bind(item: DynamicFormItem) {
            val adapter = ArrayAdapter(
                spinner.context,
                android.R.layout.simple_spinner_item,
                item.options!!
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(0)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    item.value = item.options!![position]
                    itemChangeListener.onItemClicked(item)
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    inner class RadioButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val radioGroup: RadioGroup = view.findViewById(R.id.dynamicRB)
        fun bind(item: DynamicFormItem) {
            radioGroup.removeAllViews()
            item.options!!.forEach { option ->
                val radioButton = RadioButton(radioGroup.context)
                radioButton.text = option
                radioButton.isChecked = option == item.value
                radioButton.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        item.value = option
                        itemChangeListener.onItemClicked(item)
                    }
                }
                radioGroup.addView(radioButton)
            }
        }
    }
}